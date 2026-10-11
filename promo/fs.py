import sys, statistics as st
lines=open(sys.argv[1]).read().split('\n'); hdr=None; rows=[]
for l in lines:
    if l.startswith('Flags,'): hdr=l.strip().strip(',').split(','); continue
    if hdr and l and l[0].isdigit():
        v=l.strip().strip(',').split(',')
        if len(v)>=len(hdr)-1: rows.append(dict(zip(hdr,map(int,v))))
seq=['IntendedVsync','HandleInputStart','AnimationStart','PerformTraversalsStart','DrawStart','SyncQueued','SyncStart','IssueDrawCommandsStart','SwapBuffers','FrameCompleted']
for a,b in zip(seq,seq[1:]): print(f'{a:>24} -> {b:<24} {st.median([(r[b]-r[a])/1e6 for r in rows if r[a] and r[b]]):7.2f} ms')
ts=[r['IntendedVsync'] for r in rows]; print('frames',len(rows),'fps', 1000/((ts[-1]-ts[0])/1e6/(len(ts)-1)))
