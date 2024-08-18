package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
	 public static String getToDate(LocalDate localDate) {
		   ZoneId zoneId = ZoneId.of("Africa/Cairo");
	        LocalDateTime localDateTime =   localDate.atStartOfDay();
	        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
	        ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
	        
	        return utcDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
	    }

   
}
