package com.futsch1.medtimer;

import static com.futsch1.medtimer.ActivityCodes.EXTRA_REMINDER_ID;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.futsch1.medtimer.database.Medicine;
import com.futsch1.medtimer.database.MedicineRepository;
import com.futsch1.medtimer.database.Reminder;
import com.futsch1.medtimer.database.ReminderEvent;

import java.time.Instant;

public class ReminderWork extends Worker {
    public ReminderWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i("Reminder", "Do reminder work");
        Data inputData = getInputData();

        MedicineRepository medicineRepository = new MedicineRepository((Application) getApplicationContext());
        Reminder reminder = medicineRepository.getReminder(inputData.getInt(EXTRA_REMINDER_ID, 0));
        if (reminder != null) {
            Medicine medicine = medicineRepository.getMedicine(reminder.medicineRelId);
            ReminderEvent reminderEvent = new ReminderEvent();
            reminderEvent.reminderId = reminder.reminderId;
            reminderEvent.raisedTimestamp = Instant.now().getEpochSecond();
            reminderEvent.amount = reminder.amount;
            reminderEvent.medicineName = medicine.name;
            reminderEvent.status = ReminderEvent.ReminderStatus.RAISED;

            reminderEvent.reminderEventId = (int) medicineRepository.insertReminderEvent(reminderEvent);

            Notifications.showNotification(getApplicationContext(), medicine.name, reminder.amount, reminderEvent.reminderEventId);
            Log.i("Reminder", String.format("Show reminder for %s", reminderEvent.medicineName));
            return Result.success();

        } else {
            Log.e("Reminder", "Could not find reminder in database");
            return Result.failure();
        }
    }
}
