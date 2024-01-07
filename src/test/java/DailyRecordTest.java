import com.newideas.balancebit.app.Activity;
import com.newideas.balancebit.app.DailyRecord;
import com.newideas.balancebit.app.Main;
import com.newideas.balancebit.app.enums.ActivityType;
import com.newideas.balancebit.app.enums.DayState;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;


public class DailyRecordTest {
    @Test
    public void genericTest() {
        Main.wristDeviceUUID = UUID.randomUUID();

        DailyRecord dailyRecord = new DailyRecord();
        Assert.assertNotNull("dayUUID should not be null", dailyRecord.getDayID());
        Assert.assertTrue("date should be today", dailyRecord.getDate().isEqual(java.time.LocalDate.now()));
        Assert.assertNotNull("dayType should be valid", dailyRecord.getDayType());
        Assert.assertNotNull("dayState should be valid", dailyRecord.getDayState());

        Assert.assertEquals("dayState should be empty if there are no activities", dailyRecord.getDayState(), DayState.empty);
    }

    @Test
    public void testWithOneActivity() {
        Main.wristDeviceUUID = UUID.randomUUID();

        DailyRecord dailyRecord = new DailyRecord();
        dailyRecord.addActivity(new Activity(ActivityType.sleep, LocalDateTime.now(), LocalDateTime.now().plusHours(8)));
        Assert.assertEquals("dayState should be withActivities if there is one activity", dailyRecord.getDayState(), DayState.withActivities);
    }

    @Test
    public void testWithMultipleActivities() {
        Main.wristDeviceUUID = UUID.randomUUID();

        DailyRecord dailyRecord = new DailyRecord();

        for (int i = 0; i < 10; i++) {
            dailyRecord.startNewActivity(ActivityType.sleep);
        }

        Assert.assertEquals("dayState should be withActivities if there are multiple activities", dailyRecord.getDayState(), DayState.withActivities);
    }

    @Test
    public void testAfterRemovingActivities() {
        Main.wristDeviceUUID = UUID.randomUUID();

        DailyRecord dailyRecord = new DailyRecord();
        dailyRecord.addActivity(new Activity(ActivityType.sleep, LocalDateTime.now(), LocalDateTime.now().plusHours(8)));
        Assert.assertEquals("dayState should be withActivities if there is one activity", dailyRecord.getDayState(), DayState.withActivities);

        dailyRecord.removeActivity(dailyRecord.getActivities().iterator().next());

        Assert.assertEquals("dayState should be empty if there are no activities", dailyRecord.getDayState(), DayState.empty);
    }

    @Test
    public void archiveDailyRecord() {
        Main.wristDeviceUUID = UUID.randomUUID();

        DailyRecord dailyRecord = new DailyRecord();
        dailyRecord.addActivity(new Activity(ActivityType.sleep, LocalDateTime.now(), LocalDateTime.now().plusHours(8)));
        dailyRecord.archive();
        Assert.assertEquals("dayState should be archived if the record is archived", dailyRecord.getDayState(), DayState.archived);
    }

    @Test
    public void testGettingTotalActivityTime() {
        Main.wristDeviceUUID = UUID.randomUUID();

        DailyRecord dailyRecord = new DailyRecord();
        dailyRecord.addActivity(new Activity(ActivityType.sleep, LocalDateTime.now(), LocalDateTime.now().plusHours(8)));
        dailyRecord.addActivity(new Activity(ActivityType.work, LocalDateTime.now().plusHours(8), LocalDateTime.now().plusHours(14)));
        dailyRecord.addActivity(new Activity(ActivityType.leisure, LocalDateTime.now().plusHours(14), LocalDateTime.now().plusHours(16)));
        dailyRecord.addActivity(new Activity(ActivityType.sleep, LocalDateTime.now().plusHours(16), LocalDateTime.now().plusHours(24)));

        Assert.assertEquals("total activity time should be 16 hours", dailyRecord.getTotalActivityTime(ActivityType.sleep), Duration.ofHours(16));
        Assert.assertEquals("total activity time should be 6 hours", dailyRecord.getTotalActivityTime(ActivityType.work), Duration.ofHours(6));
        Assert.assertEquals("total activity time should be 2 hours", dailyRecord.getTotalActivityTime(ActivityType.leisure), Duration.ofHours(2));
    }
}