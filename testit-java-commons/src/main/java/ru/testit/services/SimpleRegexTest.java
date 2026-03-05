import java.util.regex.Pattern;
import java.util.regex.Matcher;

class SimpleRegexTest {
    static String testCases = 
        "1. Простой тег: <div class=\"container\">Текст</div>\n" +
        "2. Одиночный самозакрывающийся: <br/> или <img src=\"img.jpg\" />\n" +
        "3. Атрибуты с одинарными кавычками: <input type='text' value='Привет'>\n" +
        "4. СЛОЖНЫЙ КЕЙС (JS в атрибуте): <button onclick=\"if(5 > 3) alert('OK');\" data-info=\"<> \">Нажми</button>\n" +
        "   // Твоя 2-я регулярка здесь захватит 'if(5 >' как конец тега.\n" +
        "5. Переносы строк внутри тега:\n" +
        "<div \n" +
        "   id=\"main\" \n" +
        "   style=\"color:red\">\n" +
        "   Content\n" +
        "</div>\n" +
        "6. Комментарии и спец-теги: и <!DOCTYPE html>\n" +
        "7. Вложенность (тег в тексте): <span>Вывод команды <b>ls -la</b> выполнен</span>\n" +
        "8. Ложное срабатывание (математика): if (a < b && c > d) { return true; }\n" +
        "   // Хорошая регулярка не должна считать это тегом.\n" +
        "9. Атрибут без значения: <input disabled type=\"checkbox\" checked>\n" +
        "10. Реальный лог из твоего примера:\n" +
        "throwable: Invalid element state... <input onchange=\"stopEvent(event)\" type=\"text\" onclick=\"ComboBox_DownClick(this);\"> ...";

    public static void main(String[] args) {
        testRegex(testCases);
    }


    public static void testRegex(String input) {
        String[] patterns = {
            "<\\S.*?(?:>|/>)",             // старая
            "<\\S.*[^>]*>",                // новая
            "<[a-zA-Z!/][^<>\"']*(?:\"[^\"]*\"[^<>\"']*|'[^']*'[^<>\"']*)*>" //(безопасная)
        };
    
        for (int i = 0; i < patterns.length; i++) {
            System.out.println("=== Тестирую паттерн #" + (i + 1) + " ===");
            Pattern p = Pattern.compile(patterns[i]);
            Matcher m = p.matcher(input);
            int count = 0;
            while (m.find()) {
                System.out.println("Найдено [" + (++count) + "]: " + m.group());
            }
            System.out.println();
        }
    }
}