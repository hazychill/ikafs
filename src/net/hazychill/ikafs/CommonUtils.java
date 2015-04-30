package net.hazychill.ikafs;

import java.util.Calendar;
import java.util.Date;

public class CommonUtils {
	public static Date calcExpireDate(int expireDays) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -expireDays);
		Date expireDate = cal.getTime();
		return expireDate;
	}
}
