package com.newideas.balancebit.app;

import com.newideas.balancebit.app.enums.ActivityType;
import com.newideas.balancebit.app.interfaces.I_Record;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;


public class Activity implements I_Record {
	private String activityID;
	@Getter private ActivityType activityType;
	@Getter private LocalDateTime startTime;
	@Getter @Setter private LocalDateTime endTime;

	/**
	 * Activity constructor that defaults to current time now.
	 * UUID is automatically created.
	 * @param type The type of activity.
	 */
	public Activity(ActivityType type) {
		LocalDateTime startTime = LocalDateTime.now();

		this.activityID = String.valueOf(startTime).concat(String.valueOf(type));
		this.activityType = type;
		this.startTime = startTime;
	}

	/**
	 * Activity constructor that requires all inputs.
	 * UUID is automatically created.
	 * @param type The type of activity.
	 * @param startTime The starting time of the activity.
	 * @param endTime The ending time of the activity.
	 */
	public Activity(ActivityType type, LocalDateTime startTime, LocalDateTime endTime) {
		this.activityID = String.valueOf(startTime).concat(String.valueOf(type));
		this.activityType = type;

		if (startTime != null) this.startTime = startTime;
		else this.startTime = LocalDateTime.now();

		if (endTime != null) {
			this.endTime = endTime;
			if (endTime.isBefore(startTime))
				throw new IllegalArgumentException("Activity cannot end before starting!");
		}
	}

	@Override
	public void construct(@NotNull ResultSet rs) throws SQLException {
		this.activityID = rs.getString("activityID");
		this.activityType = ActivityType.valueOf(rs.getString("activityType"));
		this.startTime = LocalDateTime.parse(rs.getString("startTime"));
		this.endTime = rs.getString("endTime") != null ? LocalDateTime.parse(rs.getString("endTime")) : null;
	}

	@Override
	public String getExecuteSql() {
		return String.format("INSERT INTO Activity (activityID, activityType, startTime, endTime) VALUES ('%s', '%s', '%s', '%s')",
				this.activityID, this.activityType, this.startTime, this.endTime);
	}

	@Override
	public String getSelectSql() {
		return String.format("SELECT * FROM Activity WHERE activityID = '%s'", this.activityID);
	}

	@Override
	public Boolean handleResult(ResultSet rs) {
		return null;
	}
}