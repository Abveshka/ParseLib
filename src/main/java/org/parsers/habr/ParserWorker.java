package org.parsers.habr;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.parsers.common.html.HtmlLoader;
import org.parsers.common.html.Parser;
import org.parsers.common.settings.ParserSettings;

import java.io.IOException;
import java.util.ArrayList;

@RequiredArgsConstructor
public class ParserWorker<T> {
    public interface OnNewDataHandler<T> {
        void onNewData(Object sender, T data);
    }

    public interface OnCompleted {
        void onCompleted(Object sender);
    }

     final Parser<T> parser;
     ParserSettings parserSettings;
     HtmlLoader loader;
     volatile boolean isActive;

     final ArrayList<OnNewDataHandler<T>> onNewDataList = new ArrayList<>();
     final ArrayList<OnCompleted> onCompletedList = new ArrayList<>();

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

    private void worker() throws IOException {
        for (int pageNumber = parserSettings.getStartPoint(); pageNumber <= parserSettings.getEndPoint(); pageNumber++) {
            if (!isActive) {
                notifyCompleted();
                return;
            }
            val document = loader.getSourceByPageId(pageNumber);
            val result = parser.parse(document);
            notifyNewData(result);
        }
        notifyCompleted();
        isActive = false;
    }


    private void notifyNewData(T result) {
        for (val handler : onNewDataList) {
            handler.onNewData(this, result);
        }
    }

    private void notifyCompleted() {
        for (val handler : onCompletedList) {
            handler.onCompleted(this);
        }
    }
}