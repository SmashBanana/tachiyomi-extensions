package eu.kanade.tachiyomi.extension.zh.bainianmanga

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class BainianManga : ParsedHttpSource() {

    override val name = "百年漫画"
    override val baseUrl = "https://m.bnmanhua.com"
    override val lang = "zh"
    override val supportsLatest = true

    override fun popularMangaRequest(page: Int) = GET("$baseUrl/page/hot/$page.html", headers)
    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/page/new/$page.html", headers)
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/index.php?m=vod-search-pg-$page-wd-$query.html".toHttpUrlOrNull()?.newBuilder()
        return GET(url.toString(), headers)
    }

    override fun mangaDetailsRequest(manga: SManga) = GET(baseUrl + manga.url, headers)
    override fun chapterListRequest(manga: SManga) = mangaDetailsRequest(manga)
    override fun pageListRequest(chapter: SChapter) = GET(baseUrl + chapter.url, headers)

    override fun popularMangaSelector() = "ul.tbox_m > li.vbox"
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun searchMangaSelector() = popularMangaSelector()
    override fun chapterListSelector() = "ul.list_block > li"

    override fun searchMangaNextPageSelector() = "a.pagelink_a"
    override fun popularMangaNextPageSelector() = searchMangaNextPageSelector()
    override fun latestUpdatesNextPageSelector() = searchMangaNextPageSelector()

    override fun headersBuilder() = super.headersBuilder()
        .add("Referer", baseUrl)

    override fun popularMangaFromElement(element: Element) = mangaFromElement(element)
    override fun latestUpdatesFromElement(element: Element) = mangaFromElement(element)
    override fun searchMangaFromElement(element: Element) = mangaFromElement(element)
    private fun mangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        element.select("a.vbox_t").first().let {
            manga.setUrlWithoutDomain(it.attr("href"))
            manga.title = it.attr("title").trim()
        }
        manga.thumbnail_url = element.select("mip-img").attr("src")
        return manga
    }

    override fun chapterFromElement(element: Element): SChapter {
        val urlElement = element.select("a")

        val chapter = SChapter.create()
        chapter.setUrlWithoutDomain(urlElement.attr("href"))
        chapter.name = urlElement.text().trim()
        return chapter
    }

    override fun mangaDetailsParse(document: Document): SManga {
        val infoElement = document.select("div.data")

        val manga = SManga.create()
        manga.description = document.select("div.tbox_js").text().trim()
        manga.author = infoElement.select("p.dir").text().substring(3).trim()
        return manga
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        return super.chapterListParse(response).asReversed()
    }

    override fun pageListParse(document: Document): List<Page> {
        val html = document.html()
        val baseImgUrl = "https://img.hltongchen.com/"

        val imgUrlRegex = Regex("var z_img='(.*?)';")
        val imgUrlArray = imgUrlRegex.find(html)?.groups?.get(1)?.value
        if (imgUrlArray != null) {
            val imgUrlList = Json.decodeFromString<List<String>>(imgUrlArray)
            return imgUrlList.mapIndexed { i, imgUrl ->
                Page(i, "", "$baseImgUrl$imgUrl")
            }
        }
        return listOf()
    }

    override fun imageUrlParse(document: Document) = ""

    private class GenreFilter(genres: Array<String>) : Filter.Select<String>("Genre", genres)

    override fun getFilterList() = FilterList(
        GenreFilter(getGenreList())
    )

    private fun getGenreList() = arrayOf(
        "All"
    )
}
