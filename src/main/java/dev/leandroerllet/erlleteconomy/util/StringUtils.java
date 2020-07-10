package dev.leandroerllet.erlleteconomy.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class StringUtils {

    private static final NumberFormat FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    public static String moneyFormat(double m) {
        BigDecimal valor = new BigDecimal(m);
        return FORMAT.format(valor);
    }

}
