package com.newideas.balancebit.app;

import com.newideas.balancebit.app.enums.ActivityType;
import com.newideas.balancebit.app.enums.DayState;
import com.newideas.balancebit.app.enums.DayType;
import com.newideas.balancebit.app.interfaces.I_Record;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.UUID;


public class DailyRecord implements I_Record {
	@Getter private String dayID;
	@Getter private static DayType lastUsedDayType = DayType.off;
	@Getter private LocalDate date;
	@Getter @Setter private DayType dayType;
	@Getter private DayState dayState;
	@Getter private LinkedHashSet<Activity> activities = new LinkedHashSet<>();

	public DailyRecord() {
		this.dayID = String.valueOf(Main.wristDeviceUUID).concat(String.valueOf(date));
		this.date = LocalDate.now();
		this.dayType = lastUsedDayType;
		this.dayState = DayState.empty;
	}

	public DailyRecord(LocalDate date) {
		this.dayID = String.valueOf(Main.wristDeviceUUID).concat(String.valueOf(date));
		this.date = date;
		this.dayType = lastUsedDayType;
		this.dayState = DayState.empty;
	}

	public DailyRecord(DayType dayType) {
		this.dayID = String.valueOf(Main.wristDeviceUUID).concat(String.valueOf(date));
		this.date = LocalDate.now();
		this.dayType = dayType;
		this.dayState = DayState.empty;
	}

	public DailyRecord(DayType dayType, LocalDate date) {
		this.dayID = String.valueOf(Main.wristDeviceUUID).concat(String.valueOf(date));
		this.date = date;
		this.dayType = dayType;
		this.dayState = DayState.empty;
	}

	public DailyRecord(DayType dayType, @NotNull LinkedHashSet<Activity> activities) {
		this.dayID = String.valueOf(Main.wristDeviceUUID).concat(String.valueOf(date));
		this.date = LocalDate.now();
		this.dayType = dayType;
		this.activities = activities;
		this.dayState = activities.isEmpty() ? DayState.empty : DayState.withActivities;
	}

	public DailyRecord(DayType dayType, @NotNull LinkedHashSet<Activity> activities, LocalDate date) {
		this.dayID = String.valueOf(Main.wristDeviceUUID).concat(String.valueOf(date));
		this.date = date;
		this.dayType = dayType;
		this.activities = activities;
		this.dayState = activities.isEmpty() ? DayState.empty : DayState.withActivities;
	}

	@Override
	public void construct(@NotNull ResultSet rs) throws SQLException {
		this.dayID = rs.getString("dayID");
		this.date = rs.getDate("date").toLocalDate();
		this.dayType = DayType.valueOf(rs.getString("dayType"));
		this.dayState = DayState.valueOf(rs.getString("dayState"));

		String activitiesUUIDsStr = rs.getString("activities");
		this.activities = getActivitiesFromUUIDs(activitiesUUIDsStr);
	}

	public Activity startNewActivity(ActivityType type) {
		Activity lastActivity = null;

		if (!activities.isEmpty()) lastActivity = activities.stream().reduce((first, second) -> second).orElse(null);
		if (lastActivity != null) lastActivity.setEndTime(LocalDateTime.now());

		Activity newActivity = new Activity(type);
		activities.add(newActivity);
		dayState = DayState.withActivities;

		return newActivity;
	}

	public boolean archive() {
		if (dayState == DayState.archived) return false;
		else dayState = DayState.archiving;

		dayState = syncToCloud() ? DayState.archived : activities.isEmpty() ? DayState.empty : DayState.withActivities;
		return dayState == DayState.archived;
	}


	public void addActivity(Activity activity) {
		activities.add(activity);
		dayState = DayState.withActivities;
	}


	public void removeActivity(Activity activity) {
		activities.remove(activity);
		if (activities.isEmpty()) dayState = DayState.empty;
	}


	public Duration getTotalActivityTime(ActivityType type) {
		Duration totalDuration = Duration.ZERO;

		for (Activity activity : activities) {
			if (activity.getActivityType() == type) {
				LocalDateTime startTime = activity.getStartTime();
				LocalDateTime endTime = activity.getEndTime();

				Duration activityDuration = Duration.between(startTime, endTime);

				totalDuration = totalDuration.plus(activityDuration);
			}
		}

		return totalDuration;
	}

	public boolean syncToCloud() {
//		return BackendRqService.syncToCloud();
		return true;
	}

	@Override
	public String getExecuteSql() {
		return String.format("INSERT INTO DailyRecord (dayID, date, dayType, dayState, activities) VALUES ('%s', '%s', '%s', '%s', '%s');",
				dayID, date.toString(), dayType.name(), dayState.name(), activities);
	}

	@Override
	public String getSelectSql() {
		return String.format("SELECT * FROM DailyRecord WHERE dayID = '%s';", dayID);
	}

	@Override
	public Boolean handleResult(ResultSet rs) {
		if (rs == null) return false;

		try {
			if (rs.next()) {
				this.dayID = rs.getString("dayID");
				this.date = rs.getDate("date").toLocalDate();
				this.dayType = DayType.valueOf(rs.getString("dayType"));
				this.dayState = DayState.valueOf(rs.getString("dayState"));

				String activitiesUUIDsStr = rs.getString("activities");
				this.activities = getActivitiesFromUUIDs(activitiesUUIDsStr);

				return true;
			}
		} catch (Exception ignored) {}
		return false;
	}

	private @NotNull LinkedHashSet<Activity> getActivitiesFromUUIDs(String uuidsStr) {
		LinkedHashSet<Activity> activitiesSet = new LinkedHashSet<>();
		if (uuidsStr != null && !uuidsStr.isEmpty()) {
			String[] uuids = uuidsStr.split(",");
			for (String uuidStr : uuids) {
				UUID uuid = UUID.fromString(uuidStr.trim());
				Activity activity = getActivityByUUID(uuid);
				activitiesSet.add(activity);
			}
		}
		return activitiesSet;
	}

	private Activity getActivityByUUID(UUID uuid) {
		Activity activity = null;

		try {
			ResultSet rs = LocalDBService.selectSQL(String.format("SELECT * FROM Activity WHERE activityUUID = '%s'", uuid));
			if (rs.next()) {
				activity = new Activity(ActivityType.unknown);
				activity.construct(rs);

				return activity;
			}
		} catch (Exception ignored) {}

		return activity;
	}
}