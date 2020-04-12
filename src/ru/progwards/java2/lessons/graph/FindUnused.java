package ru.progwards.java2.lessons.graph;

import java.util.ArrayList;
import java.util.List;

import static ru.progwards.java2.lessons.graph.FindUnused.Mark.*;

/**
 * Задача поиска неиспользуемых объектов (купированный алгоритм Тарьяна)
 */
public class FindUnused {

    /**
     * Возможные состояния объекта в процессе поиска
     */
    enum Mark {
        /**
         * Не используется (белая вершина в алгоритме Тарьяна)
         */
        NOT_USED,
        /**
         * Посещён (серая вершина в алгоритме Тарьяна)
         */
        VISITED,
        /**
         * Используется (черная вершина в алгоритме Тарьяна)
         */
        IN_USE
    };

    /**
     * Объект дерева
     */
    class CObject {
        /**
         * Ссылки на другие объекты дерева
         */
        public List<CObject> references;
        /**
         * Пометка для алгоритма
         * @see Mark
         */
        Mark mark;
    }

    /**
     * Возвращает список неиспользуемых узлов
     * @param roots список корневых узлов (от них начнем сканирование)
     * @param objects список всех узлов графа
     * @return
     */
    public static List<CObject> find(List<CObject> roots, List<CObject> objects) {
        for (CObject o : objects)
            o.mark = NOT_USED; // белый
        for (CObject o : roots)
            if (o.mark == NOT_USED) // белый
                DFS(o);

        List<CObject> result = new ArrayList<CObject>();
        for (CObject o : objects)
            if (o.mark != NOT_USED) // не белый
                result.add(o);
        return result;
    }

    /**
     * Рекурсивный алгоритм Тарьяна - пометка всех зависимых не помеченных узлов
     * @param object стартовый узел
     */
    public static void DFS(CObject object) {
        object.mark = VISITED; // серый
        for (CObject o : object.references)
            if (o.mark == NOT_USED) // белый
                DFS(o);
        object.mark = IN_USE; // черный
    }


}
