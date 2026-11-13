package org.example;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.ArrayList;

public class ParserWorker<T> {

    public interface OnNewDataHandler<T> {
        void onNewData(Object sender, T data);
    }

    public interface OnCompleted {
        void onCompleted(Object sender);
    }

    private Parser<T> parser;                 // вытащить данные из страницы
    private ParserSettings parserSettings;    // что и откуда парсить
    private HtmlLoader loader;
    private volatile boolean isActive;

    private final ArrayList<OnNewDataHandler<T>> onNewDataList = new ArrayList<>();
    private final ArrayList<OnCompleted> onCompletedList = new ArrayList<>();

    public ParserWorker(Parser<T> parser) {
        this.parser = parser;
    }

    public Parser<T> getParser() {
        return parser;
    }

    public void setParser(Parser<T> parser) {
        this.parser = parser;
    }

    public ParserSettings getParserSettings() {
        return parserSettings;
    }

    public void setParserSettings(ParserSettings parserSettings) {
        this.parserSettings = parserSettings;
        this.loader = new HtmlLoader(parserSettings);
    }

    public void addOnNewDataHandler(OnNewDataHandler<T> handler) {
        onNewDataList.add(handler);
    }

    public void addOnCompletedHandler(OnCompleted handler) {
        onCompletedList.add(handler);
    }

    public void start() throws IOException {
        isActive = true;
        worker();
    }

    public void abort() {
        isActive = false;
    }

    private void worker() throws IOException {
        for (int i = parserSettings.getStartPoint(); i <= parserSettings.getEndPoint(); i++) {
            if (!isActive) {
                notifyCompleted();
                return;
            }
            Document document = loader.getSourceByPageId(i);
            T result = parser.parse(document);
            notifyNewData(result);
        }
        notifyCompleted();
        isActive = false;
    }


    private void notifyNewData(T result) {
        for (OnNewDataHandler<T> handler : onNewDataList) {
            handler.onNewData(this, result);
        }
    }

    private void notifyCompleted() {
        for (OnCompleted handler : onCompletedList) {
            handler.onCompleted(this);
        }
    }
}