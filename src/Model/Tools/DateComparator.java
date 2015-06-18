package Model.Tools;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by vasily on 27.05.15.
 */
public class DateComparator implements Comparator<Date> {
    @Override
    public int compare(Date o1, Date o2) {
        if (o1.before(o2))
            return -1;
        if (o1.after(o2))
            return 1;
        return 0;
    }
}
