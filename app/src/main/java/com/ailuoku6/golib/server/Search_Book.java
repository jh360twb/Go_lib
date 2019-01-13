package com.ailuoku6.golib.server;

import com.ailuoku6.golib.Model.Book;
import com.ailuoku6.golib.Model.MuseumCollection;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Search_Book {
    private String Api = "http://www.lib.wust.edu.cn:8780/opac/openlink.php";
    private List<Book> books;

    public Search_Book(){
        books = new ArrayList<>();
    }

    public List<Book> GetBooks(String keyWord) throws IOException {
        Document document;
        Connection connection = Jsoup.connect(Api);
        connection.timeout(5*1000)
                .method(Connection.Method.GET)
                .data("strSearchType","title")
                .data("strText",keyWord);
        Connection.Response response = connection.execute();
        document = response.parse();
        Elements elements = document.select(".book_list_info");
        for (Element e : elements) {
            Book book = new Book(e.select("b").text()+e.select("h3").text(),e.select("h3 > a").attr("href"),
                    e.select("p > span").eachText().get(0),e.select("p > span").eachText().get(1),
                    e.select("p").eachText().get(0),e.select("p").eachText().get(1));
            books.add(book);
        }
        return books;
    }

}
