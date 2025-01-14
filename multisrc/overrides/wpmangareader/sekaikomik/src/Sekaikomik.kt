package eu.kanade.tachiyomi.extension.id.sekaikomik

import eu.kanade.tachiyomi.annotations.Nsfw
import eu.kanade.tachiyomi.multisrc.wpmangareader.WPMangaReader
import java.text.SimpleDateFormat
import java.util.Locale

@Nsfw
class Sekaikomik : WPMangaReader("Sekaikomik", "https://www.sekaikomik.in", "id", dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("id")))
