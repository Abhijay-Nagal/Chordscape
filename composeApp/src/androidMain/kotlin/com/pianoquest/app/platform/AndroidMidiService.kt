package com.pianoquest.app.platform

import android.content.Context
import android.content.pm.PackageManager
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiDeviceStatus
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.pianoquest.app.input.MidiByteParser
import com.pianoquest.app.input.MidiDevice
import com.pianoquest.app.input.MidiMessage
import com.pianoquest.app.input.MidiService
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * USB (and system-paired Bluetooth) MIDI keyboards through android.media.midi.
 *
 * Robustness measures (a flaky OTG cable or a keyboard that briefly resets used to leave the app "deaf"):
 *  - every output port of the keyboard is opened, not just port 0;
 *  - when the device disappears and comes back it is reconnected automatically;
 *  - a watchdog re-opens the connection if Android silently closes it;
 *  - all held notes and the sustain pedal are released on disconnect so nothing gets stuck.
 */
class AndroidMidiService(private val context: Context) : MidiService {
    private val manager: MidiManager? =
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI))
            context.getSystemService(Context.MIDI_SERVICE) as? MidiManager else null
    private val handler = Handler(Looper.getMainLooper())

    override val supported: Boolean get() = manager != null
    override val devices = MutableStateFlow<List<MidiDevice>>(emptyList())
    override val connected = MutableStateFlow<MidiDevice?>(null)
    override val status = MutableStateFlow(if (manager == null) "This device does not support MIDI" else "No MIDI keyboard connected")
    override val lastActivity = MutableStateFlow(0L)

    private var listener: (MidiMessage) -> Unit = {}
    private val parser = MidiByteParser { listener(it) }
    private var openDevice: android.media.midi.MidiDevice? = null
    private val openPorts = ArrayList<MidiOutputPort>()
    private val infos = HashMap<String, MidiDeviceInfo>()
    /** The device the user wants; reconnected automatically when it reappears. */
    private var wanted: MidiDevice? = null
    private var opening = false

    private val receiver = object : MidiReceiver() {
        override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
            lastActivity.value = System.currentTimeMillis()
            synchronized(parser) { parser.feed(msg, offset, count) }
        }
    }

    private val watchdog = object : Runnable {
        override fun run() {
            val w = wanted
            if (w != null && connected.value == null && !opening) {
                refresh()
                devices.value.firstOrNull { it.id == w.id || it.name == w.name }?.let { connect(it) }
            }
            handler.postDelayed(this, 3000)
        }
    }

    init {
        manager?.let { m ->
            val cb = object : MidiManager.DeviceCallback() {
                override fun onDeviceAdded(device: MidiDeviceInfo) {
                    refresh()
                    val model = toModel(device)
                    val w = wanted
                    if (device.outputPortCount > 0 && (connected.value == null) && (w == null || w.name == model.name)) connect(model)
                }

                override fun onDeviceRemoved(device: MidiDeviceInfo) {
                    if (connected.value?.id == device.id.toString()) closeConnection("${connected.value?.name} was unplugged - waiting for it to come back…")
                    refresh()
                }

                override fun onDeviceStatusChanged(status: MidiDeviceStatus) {
                    // Some keyboards reset their USB interface; make sure we're still listening.
                    if (connected.value?.id == status.deviceInfo.id.toString() && openPorts.isEmpty()) connect(connected.value!!)
                }
            }
            if (Build.VERSION.SDK_INT >= 33) m.registerDeviceCallback(MidiManager.TRANSPORT_MIDI_BYTE_STREAM, { handler.post(it) }, cb)
            else @Suppress("DEPRECATION") m.registerDeviceCallback(cb, handler)
        }
        refresh()
        devices.value.firstOrNull()?.let { connect(it) }
        handler.postDelayed(watchdog, 3000)
    }

    override fun setListener(listener: (MidiMessage) -> Unit) { this.listener = listener }

    private fun currentInfos(): Collection<MidiDeviceInfo> {
        val m = manager ?: return emptyList()
        return if (Build.VERSION.SDK_INT >= 33) m.getDevicesForTransport(MidiManager.TRANSPORT_MIDI_BYTE_STREAM)
        else @Suppress("DEPRECATION") m.devices.toList()
    }

    override fun refresh() {
        if (manager == null) return
        infos.clear()
        val list = currentInfos().filter { it.outputPortCount > 0 }.map { toModel(it) }
        devices.value = list
        if (connected.value == null && wanted == null) {
            status.value = if (list.isEmpty()) "No MIDI keyboard found. Plug one in with a USB-OTG cable." else "Keyboard found - tap Connect"
        }
    }

    private fun toModel(info: MidiDeviceInfo): MidiDevice {
        val props = info.properties
        val name = props.getString(MidiDeviceInfo.PROPERTY_NAME)
            ?: props.getString(MidiDeviceInfo.PROPERTY_PRODUCT) ?: "MIDI device ${info.id}"
        val maker = props.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER) ?: ""
        val type = when (info.type) {
            MidiDeviceInfo.TYPE_USB -> "USB"
            MidiDeviceInfo.TYPE_BLUETOOTH -> "Bluetooth"
            else -> "Virtual"
        }
        infos[info.id.toString()] = info
        return MidiDevice(info.id.toString(), name, listOf(maker, type).filter { it.isNotBlank() }.joinToString(" • "))
    }

    override fun connect(device: MidiDevice) {
        val m = manager ?: return
        val info = infos[device.id] ?: currentInfos().firstOrNull { it.id.toString() == device.id }?.also { toModel(it) } ?: return
        closeConnection(null)
        wanted = device
        opening = true
        status.value = "Connecting to ${device.name}…"
        m.openDevice(info, { opened ->
            opening = false
            if (opened == null) {
                status.value = "Could not open ${device.name} - check the cable, then tap Reconnect"
                return@openDevice
            }
            openDevice = opened
            for (i in 0 until info.outputPortCount) {
                runCatching { opened.openOutputPort(i) }.getOrNull()?.let { port ->
                    port.connect(receiver)
                    openPorts += port
                }
            }
            if (openPorts.isEmpty()) {
                status.value = "${device.name} has no readable port"
                return@openDevice
            }
            connected.value = device
            status.value = "Connected: ${device.name}"
            Log.i("Keyscape", "MIDI connected to ${device.name} (${openPorts.size} port(s))")
        }, handler)
    }

    private fun closeConnection(message: String?) {
        for (p in openPorts) runCatching { p.disconnect(receiver); p.close() }
        openPorts.clear()
        runCatching { openDevice?.close() }
        openDevice = null
        if (connected.value != null) listener(MidiMessage.AllNotesOff)
        connected.value = null
        if (message != null) status.value = message
    }

    override fun reconnect() {
        val w = wanted ?: devices.value.firstOrNull() ?: run { refresh(); return }
        refresh()
        val target = devices.value.firstOrNull { it.id == w.id || it.name == w.name } ?: run {
            status.value = "${w.name} not found - check the cable"
            return
        }
        connect(target)
    }

    override fun disconnect() {
        wanted = null
        closeConnection("Disconnected")
    }
}
