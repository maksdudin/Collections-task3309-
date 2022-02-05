package com.javarush.task.task33.task3309;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/* 
Комментарий внутри xml
*/

public class Solution {

    private static String[] escapeSymbols = {"<", ">", "'", "\"", "&"};

    public static String toXmlWithComment(Object obj, String tagName, String comment) {// на входе объект класса,
        // имя элемента и строка коментария
        try {
            return addCommentToTag(convertObjectToXML(obj), tagName, comment);//вызываем метод и возвращаем результат его работы
            // причём на вход метода должна первым аргументом подаваться xml строка нашего сериализованного объекта
            // которая вернётся при вызове метода convertObjectToXML(obj) который и произведёт серилизацию
        } catch (Exception ignored) {// если не получилось эксепшены игнорируем
            ignored.toString();
        }
        return null;//и возвращаем null
    }
    // ок на входе xml строка -первым аргументом, название элемента, и строка которую нужно добавить в качестве комментария
    private static String addCommentToTag(String xml, String tagName, String comment) throws Exception {
        Document document = getDocument(xml);// начинается DOM Document Object Model - создали структуру Document,
        // причем getDocument(xml)- это не метод из org.w3c.dom, а написанный нами метод (он ниже по коду) он возвращает
        // структуру Document распарщенную из потока байтов в который преобразовали xml строку
        document.normalizeDocument();//что как-то там нормализует =( не совемс понятно что и как
        // добавляем элементу информация в котором содержит спец символы xml разметки CDATA  информацию позволяющую
        // компилятору их игнорировать
        addCdataBlocks(document, document.getDocumentElement());//document.getDocumentElement() - возвращает корневой узел
        // теперь добавим комментарии
        addComments(tagName, comment, document);

        // потом опять сериализуем имеемый document
        StringWriter writer = new StringWriter();// замутили StringWriter
        Transformer transformer = TransformerFactory.newInstance().newTransformer();//Экземпляр этого абстрактного
        // класса может преобразовать исходное дерево в дерево результата.
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");//indent указывает, может ли Transformer добавлять
        // дополнительные пробелы при выводе результирующего дерева; значение должно быть да или нет.( а я то думал
        // откуда эти разрывы берутся)
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");//standalone указывает, должен ли Transformer
        // выводить автономную декларацию документа; значение должно быть да или нет
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    private static void addCdataBlocks(Document document, Node rootElement) {
        if (rootElement.hasChildNodes()) {// если корневой элемент имеет дочерние?, то
            NodeList childNodes = rootElement.getChildNodes();// создаём список дочених элементов
            int length = childNodes.getLength();// длинна списка
            for (int i = 0; i < length; i++) {// пробегаем по всему списку и
                addCdataBlocks(document, childNodes.item(i));// вызываем метод в на вход которого подаём сам документ
                // и его дочерний элемент (рекурсия), если у дочернего тоже есть дочерний опять вызываем этот же метод
                // пока у какого-то элемента дочерних не будет и тогда переходим в блок else
            }
        } else {// а тут
            String textContent = rootElement.getTextContent();//getTextContent()-его атрибут возвращает текстовое
            // содержимое этого узла и его потомков, но потомков у нас нет (иначе бы всё скомкалось в одну строку - всё
            // текстовое содержимое)

            if (containsEscapeSymbols(textContent)) {// если метод containsEscapeSymbols вернёт true,( а вернёт он true
                //  в случае если строка не null и содержит буквы из массива escapeSymbols все эти "<", ">", "'", "\"", "&")

                rootElement.setTextContent("");// то записываем пустую строку  т.е стираем текстовое содержимое
                rootElement.getParentNode().appendChild(document.createCDATASection(textContent));// затем поднимаемся
                // на один уровень вверх  и добавляем новый дочерний элемент содержащий тот же текст, но уже как CDATA
                // данные, т.е данные элемента second содержащие текст (имеющий служебные элементы), которые не будут
                // интерпретироваться как разметка XML, а поскольку дочерний элемент (т.е к тому из которого всё только
                // что стерли) уже был он удаляется.
                //
            }
        }
    }


    private static void addComments(String tagName, String comment, Document document) {
        NodeList nodeList = document.getElementsByTagName(tagName);//создали список элементов с именем тэга tagName
        for (int i = 0; i < nodeList.getLength(); i++) {// "прошлись по нему"  в цикле
            Comment documentComment = document.createComment(comment);// создали объект комментарий
            documentComment.normalize();// тоже там как-то нормализовали его
            Node item = nodeList.item(i);// создали объект класса Node и присвоили ему элемент из списка nodeList
            item.getParentNode().insertBefore(documentComment, item);//переходим на родительский уровень и вставляем узел
            // documentComment  - который  у нас комментарий перед дочерним узлом item
        }
    }
// аааа игище басурманское то-то я не мог найти его в доках
    private static Document getDocument(String xml) throws Exception {// на входе xml строка с нашим сериализованным
        // объектом класса First
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();//// Получение фабрики, чтобы
        // после получить билдер документов. все по лекции
        builderFactory.setNamespaceAware(true);//Указывает, что синтаксический анализатор, создаваемый этим кодом,
        // будет обеспечивать поддержку пространств имен XML. по умолчанию false  у нас true  а зачем?
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();// Получили из фабрики билдер, который
        // парсит XML, создает структуру Document в виде иерархического дерева.
        return documentBuilder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
  //распарсили (с помощью кодировки UTF_8) xml строку побайтно во внутенний буфер и организовали поток на чтение из него
    }

// а вот этот метод и производит сериализацию нашего объекта поданного на вход
    private static String convertObjectToXML(Object o) throws Exception {
        StringWriter writer = new StringWriter();// организуем StringWriter
        // классический вариант из лекции
        /*JAXBContext context = JAXBContext.newInstance(Cat.class);
        Marshaller marshaller = context.createMarshaller();*/
        // ниже запись в одну строку
        Marshaller marshaller = JAXBContext.newInstance(o.getClass()).createMarshaller();//o.getClass() - Получение
        // «объект класса» у объекта типа Object.
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);//FORMATTED_OUTPUT в TRUE. В результат
        // будут добавлены переносы строки и пробелы, чтобы код был читабельным для человека, а не весь текст в одну строку.
        marshaller.marshal(o, writer);//сериализуем записываем во writer

        return writer.toString();// возвращаем xml строку (что и требовалось)
    }
// проверка строки на то что не пустая и содержит буквы из списка escapeSymbols)
    private static boolean containsEscapeSymbols(String s) {
        if (s == null || s.isEmpty()) {// если с трока равна null false
            return false;
        } else {
            for (String character : escapeSymbols) {
                if (s.contains(character))
                    return true;
            }
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        First obj = new First();//делаем объект класса
        System.out.println(toXmlWithComment(obj, "second", "it's a comment"));// вызываем метод и выводм его результат на печать
    }

    @XmlType(name = "first")//указывает на то, что класс участвует в JAXB сериализации, в ней же задано имя, которое будет у XML-тега для этого класса.
    @XmlRootElement//что этот объект может быть «корнем дерева» элементов в XML
    public static class First {// создаём класс first что бы было что сериализовывать
        @XmlElement(name = "second", type = String.class)//Поле будет представлено в XML-элементом.Позволяет задать имя для тэга.
        public String[] needCDATA = new String[]{"some string","some string","need CDATA because of < and >", ""};
// не используем вообще нигде и никак потому что нет аннотаций
        public List<String> characters = new ArrayList<>();
    }

}
