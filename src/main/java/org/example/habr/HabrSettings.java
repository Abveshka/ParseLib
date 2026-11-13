package org.example.habr;
import org.example.ParserSettings;

public class HabrSettings extends ParserSettings{
    public HabrSettings(int start, int end){
        startPoint = start;
        endPoint = end;
        baseUrl = "https://habr.com/ru/all";
        prefix = "page{CurrentId}";
    }
}