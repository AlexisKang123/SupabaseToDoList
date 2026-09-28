package br.com.todolistsupabase.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Toda conversão de data do app fica aqui.
 * Tela: dd/MM/yyyy  |  Supabase: yyyy-MM-dd (aceito por colunas date e timestamp).
 * SimpleDateFormat não é thread-safe, por isso criamos um novo a cada uso.
 */
public final class DataUtils {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final String FORMATO_TELA = "dd/MM/yyyy";
    private static final String FORMATO_SUPABASE = "yyyy-MM-dd";

    private DataUtils() {
    }

    /** "2026-09-27" ou "2026-09-27T00:00:00+00:00" -> "27/09/2026". */
    public static String paraTela(String dataSupabase) {

        if (dataSupabase == null || dataSupabase.length() < 10) {
            return "";
        }

        Date data = ler(dataSupabase.substring(0, 10), FORMATO_SUPABASE);
        return data == null ? "" : formatar(data, FORMATO_TELA);
    }

    /** "27/09/2026" -> "2026-09-27". Retorna null se vazia ou inválida. */
    public static String paraSupabase(String dataTela) {

        if (dataTela == null || dataTela.trim().isEmpty()) {
            return null;
        }

        Date data = ler(dataTela.trim(), FORMATO_TELA);
        return data == null ? null : formatar(data, FORMATO_SUPABASE);
    }

    /** "27/09/2026" -> Calendar. Se inválida, retorna hoje. */
    public static Calendar paraCalendar(String dataTela) {

        Calendar calendar = Calendar.getInstance();
        Date data = dataTela == null ? null : ler(dataTela.trim(), FORMATO_TELA);

        if (data != null) {
            calendar.setTime(data);
        }

        return calendar;
    }

    public static String formatarTela(int ano, int mes, int dia) {
        // O mês do DatePicker começa em 0
        return String.format(PT_BR, "%02d/%02d/%04d", dia, mes + 1, ano);
    }

    /** "27 de setembro de 2026". */
    public static String hojePorExtenso() {
        return formatar(new Date(), "dd 'de' MMMM 'de' yyyy");
    }

    private static Date ler(String texto, String padrao) {
        SimpleDateFormat formato = new SimpleDateFormat(padrao, PT_BR);
        formato.setLenient(false); // rejeita datas como 31/02
        try {
            return formato.parse(texto);
        } catch (ParseException e) {
            return null;
        }
    }

    private static String formatar(Date data, String padrao) {
        return new SimpleDateFormat(padrao, PT_BR).format(data);
    }
}
