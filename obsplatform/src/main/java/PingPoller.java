import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.springframework.format.number.NumberFormatter;


public class PingPoller
{
    public static void main(String[] args) throws ParseException
    {

        String source = "12.125,00".trim();
           Locale locale=new Locale("is");
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        DecimalFormat df = (DecimalFormat) format;
        DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
        // http://bugs.sun.com/view_bug.do?bug_id=4510618
        char groupingSeparator = symbols.getGroupingSeparator();
        if (groupingSeparator == '\u00a0') {
            source = source.replaceAll(" ", Character.toString('\u00a0'));
        }

        NumberFormatter numberFormatter = new NumberFormatter();
        Number parsedNumber = numberFormatter.parse(source,locale);
        System.out.println(BigDecimal.valueOf(Double.valueOf(parsedNumber.doubleValue())));
    }
    
    
}