package com.pianoquest.app.ui.components

import com.pianoquest.app.generated.resources.Res
import com.pianoquest.app.generated.resources.img_albeniz
import com.pianoquest.app.generated.resources.img_art_tatum
import com.pianoquest.app.generated.resources.img_bach
import com.pianoquest.app.generated.resources.img_bach_thomaskirche
import com.pianoquest.app.generated.resources.img_beethoven
import com.pianoquest.app.generated.resources.img_beethoven_hornemann
import com.pianoquest.app.generated.resources.img_bizet
import com.pianoquest.app.generated.resources.img_brahms
import com.pianoquest.app.generated.resources.img_chopin
import com.pianoquest.app.generated.resources.img_chopin_young
import com.pianoquest.app.generated.resources.img_clara_schumann
import com.pianoquest.app.generated.resources.img_clavichord
import com.pianoquest.app.generated.resources.img_clementi
import com.pianoquest.app.generated.resources.img_cristofori_piano
import com.pianoquest.app.generated.resources.img_debussy
import com.pianoquest.app.generated.resources.img_dvorak
import com.pianoquest.app.generated.resources.img_fortepiano
import com.pianoquest.app.generated.resources.img_grand_piano
import com.pianoquest.app.generated.resources.img_grieg
import com.pianoquest.app.generated.resources.img_handel
import com.pianoquest.app.generated.resources.img_harpsichord
import com.pianoquest.app.generated.resources.img_haydn
import com.pianoquest.app.generated.resources.img_impression_sunrise
import com.pianoquest.app.generated.resources.img_joplin
import com.pianoquest.app.generated.resources.img_kuhlau
import com.pianoquest.app.generated.resources.img_liszt
import com.pianoquest.app.generated.resources.img_liszt_piano
import com.pianoquest.app.generated.resources.img_maple_leaf_cover
import com.pianoquest.app.generated.resources.img_mozart
import com.pianoquest.app.generated.resources.img_mozart_child
import com.pianoquest.app.generated.resources.img_rachmaninoff
import com.pianoquest.app.generated.resources.img_rameau
import com.pianoquest.app.generated.resources.img_satie
import com.pianoquest.app.generated.resources.img_scarlatti
import com.pianoquest.app.generated.resources.img_schubert
import com.pianoquest.app.generated.resources.img_schubertiade
import com.pianoquest.app.generated.resources.img_schumann
import com.pianoquest.app.generated.resources.img_schumanns
import com.pianoquest.app.generated.resources.img_scriabin
import com.pianoquest.app.generated.resources.img_strauss
import com.pianoquest.app.generated.resources.img_tchaikovsky
import com.pianoquest.app.generated.resources.img_tchaikovsky_young
import com.pianoquest.app.generated.resources.img_thelonious_monk
import org.jetbrains.compose.resources.DrawableResource

/** Public-domain / CC0 pictures used in lessons (Wikimedia Commons). */
object LessonImages {
    fun resource(key: String): DrawableResource? = when (key) {
        "albeniz" -> Res.drawable.img_albeniz
        "art_tatum" -> Res.drawable.img_art_tatum
        "bach" -> Res.drawable.img_bach
        "bach_thomaskirche" -> Res.drawable.img_bach_thomaskirche
        "beethoven" -> Res.drawable.img_beethoven
        "beethoven_hornemann" -> Res.drawable.img_beethoven_hornemann
        "bizet" -> Res.drawable.img_bizet
        "brahms" -> Res.drawable.img_brahms
        "chopin" -> Res.drawable.img_chopin
        "chopin_young" -> Res.drawable.img_chopin_young
        "clara_schumann" -> Res.drawable.img_clara_schumann
        "clavichord" -> Res.drawable.img_clavichord
        "clementi" -> Res.drawable.img_clementi
        "cristofori_piano" -> Res.drawable.img_cristofori_piano
        "debussy" -> Res.drawable.img_debussy
        "dvorak" -> Res.drawable.img_dvorak
        "fortepiano" -> Res.drawable.img_fortepiano
        "grand_piano" -> Res.drawable.img_grand_piano
        "grieg" -> Res.drawable.img_grieg
        "handel" -> Res.drawable.img_handel
        "harpsichord" -> Res.drawable.img_harpsichord
        "haydn" -> Res.drawable.img_haydn
        "impression_sunrise" -> Res.drawable.img_impression_sunrise
        "joplin" -> Res.drawable.img_joplin
        "kuhlau" -> Res.drawable.img_kuhlau
        "liszt" -> Res.drawable.img_liszt
        "liszt_piano" -> Res.drawable.img_liszt_piano
        "maple_leaf_cover" -> Res.drawable.img_maple_leaf_cover
        "mozart" -> Res.drawable.img_mozart
        "mozart_child" -> Res.drawable.img_mozart_child
        "rachmaninoff" -> Res.drawable.img_rachmaninoff
        "rameau" -> Res.drawable.img_rameau
        "satie" -> Res.drawable.img_satie
        "scarlatti" -> Res.drawable.img_scarlatti
        "schubert" -> Res.drawable.img_schubert
        "schubertiade" -> Res.drawable.img_schubertiade
        "schumann" -> Res.drawable.img_schumann
        "schumanns" -> Res.drawable.img_schumanns
        "scriabin" -> Res.drawable.img_scriabin
        "strauss" -> Res.drawable.img_strauss
        "tchaikovsky" -> Res.drawable.img_tchaikovsky
        "tchaikovsky_young" -> Res.drawable.img_tchaikovsky_young
        "thelonious_monk" -> Res.drawable.img_thelonious_monk
        else -> null
    }

    /** (key, credit line, source URL) */
    val credits: List<Triple<String, String, String>> = listOf(
        Triple("albeniz", "Albeniz au piano 1888.jpg - Darío de Regoyos (Public domain)", "https://commons.wikimedia.org/wiki/File:Albeniz_au_piano_1888.jpg"),
        Triple("art_tatum", "(Portrait of Art Tatum, Rochester, N.Y.(?), ca. May 1946) (LOC) (5148796890).jpg - The Library of Congress (Public domain)", "https://commons.wikimedia.org/wiki/File:(Portrait_of_Art_Tatum,_Rochester,_N.Y.(%3F),_ca._May_1946)_(LOC)_(5148796890).jpg"),
        Triple("bach", "Johann Sebastian Bach.jpg - Elias Gottlob Haussmann (Public domain)", "https://commons.wikimedia.org/wiki/File:Johann_Sebastian_Bach.jpg"),
        Triple("bach_thomaskirche", "Thomaskirche und -schule 1723.jpg - Johann Gottfried Krügner (Public domain)", "https://commons.wikimedia.org/wiki/File:Thomaskirche_und_-schule_1723.jpg"),
        Triple("beethoven", "Beethoven.jpg - Joseph Karl Stieler (Public domain)", "https://commons.wikimedia.org/wiki/File:Beethoven.jpg"),
        Triple("beethoven_hornemann", "Beethoven Hornemann.jpg - Christian Horneman (Public domain)", "https://commons.wikimedia.org/wiki/File:Beethoven_Hornemann.jpg"),
        Triple("bizet", "Georges bizet.jpg - Étienne Carjat (Public domain)", "https://commons.wikimedia.org/wiki/File:Georges_bizet.jpg"),
        Triple("brahms", "JohannesBrahms.jpg - C. Brasch, Berlin (biography) (Public domain)", "https://commons.wikimedia.org/wiki/File:JohannesBrahms.jpg"),
        Triple("chopin", "Frederic Chopin photo.jpeg - Louis-Auguste Bisson (Public domain)", "https://commons.wikimedia.org/wiki/File:Frederic_Chopin_photo.jpeg"),
        Triple("chopin_young", "Chopin, by Wodzinska.JPG - Maria Wodzińska (Public domain)", "https://commons.wikimedia.org/wiki/File:Chopin,_by_Wodzinska.JPG"),
        Triple("clara_schumann", "Clara Schumann 1878.jpg - Franz von Lenbach (Public domain)", "https://commons.wikimedia.org/wiki/File:Franz_von_Lenbach_-_Clara_Schumann_(Pastell_1878).jpg"),
        Triple("clavichord", "Clavichord MET 172212.jpg - Creator:Johann Christoph Jesse (CC0)", "https://commons.wikimedia.org/wiki/File:Clavichord_MET_172212.jpg"),
        Triple("clementi", "Muzio Clementi.jpg - Aleksander Orłowski (Public domain)", "https://commons.wikimedia.org/wiki/File:Muzio_Clementi.jpg"),
        Triple("cristofori_piano", "Grand Piano MET mi89.4.1219.AV1.jpg - Bartolomeo Cristofori (CC0)", "https://commons.wikimedia.org/wiki/File:Grand_Piano_MET_mi89.4.1219.AV1.jpg"),
        Triple("debussy", "Atelier Nadar - Claude Debussy (1862-1918), Komponist (Zeno Fotografie).jpg - Atelier Nadar (Public domain)", "https://commons.wikimedia.org/wiki/File:Atelier_Nadar_-_Claude_Debussy_(1862-1918),_Komponist_(Zeno_Fotografie).jpg"),
        Triple("dvorak", "Dvorak.jpg - Unknown authorUnknown author (Public domain)", "https://commons.wikimedia.org/wiki/File:Dvorak.jpg"),
        Triple("fortepiano", "Hammerflügel Walter MIM 5423.jpg - Museumsinsulaner (Public domain)", "https://commons.wikimedia.org/wiki/File:Hammerfl%C3%BCgel_Walter_MIM_5423.jpg"),
        Triple("grand_piano", "Grand Piano MET DT8236.jpg - Creator:Steinway &amp; Sons (CC0)", "https://commons.wikimedia.org/wiki/File:Grand_Piano_MET_DT8236.jpg"),
        Triple("grieg", "Edvard Grieg (1888) by Elliot and Fry.jpg - Elliott &amp; Fry (Public domain)", "https://commons.wikimedia.org/wiki/File:Edvard_Grieg_(1888)_by_Elliot_and_Fry.jpg"),
        Triple("handel", "George Frideric Handel by Balthasar Denner.jpg - Attributed to Balthasar Denner (Public domain)", "https://commons.wikimedia.org/wiki/File:George_Frideric_Handel_by_Balthasar_Denner.jpg"),
        Triple("harpsichord", "Harpsichord MET DT684a.jpg - Creator:Jan Couchet the Elder (CC0)", "https://commons.wikimedia.org/wiki/File:Harpsichord_MET_DT684a.jpg"),
        Triple("haydn", "Joseph Haydn.jpg - Thomas Hardy (Public domain)", "https://commons.wikimedia.org/wiki/File:Joseph_Haydn.jpg"),
        Triple("impression_sunrise", "Monet - Impression, Sunrise.jpg - Claude Monet (Public domain)", "https://commons.wikimedia.org/wiki/File:Monet_-_Impression,_Sunrise.jpg"),
        Triple("joplin", "Scott Joplin 19072.jpg - Unknown authorUnknown author (Public domain)", "https://commons.wikimedia.org/wiki/File:Scott_Joplin_19072.jpg"),
        Triple("kuhlau", "Fr. Kuhlau - Em. Baerentzen ft - btv1b84215472.jpg - Baerentzen, Emilius Ditlev (1799-1868). Lithographe (Public domain)", "https://commons.wikimedia.org/wiki/File:Fr._Kuhlau_-_Em._Baerentzen_ft_-_btv1b84215472.jpg"),
        Triple("liszt", "Franz Liszt by Herman Biow- 1843.png - Herman Biow (Public domain)", "https://commons.wikimedia.org/wiki/File:Franz_Liszt_by_Herman_Biow-_1843.png"),
        Triple("liszt_piano", "1840 Danhauser Liszt am Flügel anagoria.JPG - Josef Danhauser (Public domain)", "https://commons.wikimedia.org/wiki/File:1840_Danhauser_Liszt_am_Fl%C3%BCgel_anagoria.JPG"),
        Triple("maple_leaf_cover", "MapleLeafSedaliaPrinting99.jpg - Scott Joplin (Public domain)", "https://commons.wikimedia.org/wiki/File:MapleLeafSedaliaPrinting99.jpg"),
        Triple("mozart", "Mozart Portrait Croce.jpg - Possibly Johann Nepomuk della Croce (Public domain)", "https://commons.wikimedia.org/wiki/File:Mozart_Portrait_Croce.jpg"),
        Triple("mozart_child", "Louis Carrogis dit Carmontelle - Portrait de Wolfgang Amadeus Mozart (Salzbourg, 1756-Vienne, 1791) jouant à Paris avec son père Jean... - Google Art Project.jpg - Louis Carrogis Carmontelle (Public domain)", "https://commons.wikimedia.org/wiki/File:Louis_Carrogis_dit_Carmontelle_-_Portrait_de_Wolfgang_Amadeus_Mozart_(Salzbourg,_1756-Vienne,_1791)_jouant_%C3%A0_Paris_avec_son_p%C3%A8re_Jean..._-_Google_Art_Project.jpg"),
        Triple("rachmaninoff", "Sergei Rachmaninoff cph.3a40575.jpg - Kubey-Rembrandt Studios (Philadephia, Pennsylvania) (Public domain)", "https://commons.wikimedia.org/wiki/File:Sergei_Rachmaninoff_cph.3a40575.jpg"),
        Triple("rameau", "Attribué à Joseph Aved, Portrait de Jean-Philippe Rameau (vers 1728) - 001.jpg - Attributed to Joseph Aved / Formerly attributed to Jean-Baptiste-Siméon Chardin (Public domain)", "https://commons.wikimedia.org/wiki/File:Attribu%C3%A9_%C3%A0_Joseph_Aved,_Portrait_de_Jean-Philippe_Rameau_(vers_1728)_-_001.jpg"),
        Triple("satie", "Erik Satie - BNF1.jpeg - Unattributed (Public domain)", "https://commons.wikimedia.org/wiki/File:Erik_Satie_-_BNF1.jpeg"),
        Triple("scarlatti", "Domenico Scarlatti.jpg - Domingo Antonio Velasco (Public domain)", "https://commons.wikimedia.org/wiki/File:Retrato_de_Domenico_Scarlatti.jpg"),
        Triple("schubert", "Franz Schubert by Wilhelm August Rieder 1875.jpg - Wilhelm August Rieder (Public domain)", "https://commons.wikimedia.org/wiki/File:Franz_Schubert_by_Wilhelm_August_Rieder_1875.jpg"),
        Triple("schubertiade", "Julius Schmid Schubertiade.jpg - Julius Schmid (Public domain)", "https://commons.wikimedia.org/wiki/File:Julius_Schmid_Schubertiade.jpg"),
        Triple("schumann", "Robert Schumann 1839.jpg - Josef Kriehuber (Public domain)", "https://commons.wikimedia.org/wiki/File:Robert_Schumann_1839.jpg"),
        Triple("schumanns", "E Kaiser - Robert + Clara Schumann (Litho 1847).jpg - Eduard Kaiser (Public domain)", "https://commons.wikimedia.org/wiki/File:E_Kaiser_-_Robert_%2B_Clara_Schumann_(Litho_1847).jpg"),
        Triple("scriabin", "Alexander Scriabin (by Alexander Mozer).jpg - Александр Эдмундович Мозер (1870-1958) (Public domain)", "https://commons.wikimedia.org/wiki/File:Alexander_Scriabin_(by_Alexander_Mozer).jpg"),
        Triple("strauss", "Ifj Johann Strauss Pesti Hirlap Vasarnapja 1930 Nr50.jpg - Unknown authorUnknown author (Public domain)", "https://commons.wikimedia.org/wiki/File:Ifj_Johann_Strauss_Pesti_Hirlap_Vasarnapja_1930_Nr50.jpg"),
        Triple("tchaikovsky", "Tchaikovsky by Reutlinger (cropped).jpg - Émile Reutlinger (Public domain)", "https://commons.wikimedia.org/wiki/File:Tchaikovsky_by_Reutlinger_(cropped).jpg"),
        Triple("tchaikovsky_young", "Pyotr Tchaikovsky с. 1870.jpg - Unknown authorUnknown author (Public domain)", "https://commons.wikimedia.org/wiki/File:Pyotr_Tchaikovsky_%D1%81._1870.jpg"),
        Triple("thelonious_monk", "(Portrait of Thelonious Monk, Minton's Playhouse, New York, N.Y., ca. Sept. 1947) (LOC) (4843138893).jpg - The Library of Congress (Public domain)", "https://commons.wikimedia.org/wiki/File:(Portrait_of_Thelonious_Monk,_Minton%27s_Playhouse,_New_York,_N.Y.,_ca._Sept._1947)_(LOC)_(4843138893).jpg"),
    )
}
