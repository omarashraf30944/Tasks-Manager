package com.example.project1;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.project1.databinding.FragmentInsertBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateOrBuilder;

import java.util.Calendar;


public class InsertFragment extends BottomSheetDialogFragment {
    FragmentInsertBinding binding;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_insert ,container,false);
        return binding.getRoot();
    }


    private void openDatePicker(){
        final Calendar newCalendar = Calendar.getInstance();

        final DatePickerDialog StartTime = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                binding.date.setText(dayOfMonth + "/" + monthOfYear + "/" + year);
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        StartTime.show();
    }


    private void openTimePicker() {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;

        mTimePicker = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                binding.time.setText(selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.date.setInputType(InputType.TYPE_NULL);
        binding.date.setOnClickListener(v -> openDatePicker());
        binding.date.setOnFocusChangeListener((v, hasFocus) -> openDatePicker());

        binding.time.setInputType(InputType.TYPE_NULL);
        binding.time.setOnClickListener(v -> openTimePicker());
        binding.time.setOnFocusChangeListener((v, hasFocus) -> openTimePicker());


        binding.insertTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = binding.title.getText().toString();

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "title required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String date = binding.date.getText().toString();

                if (date.isEmpty()) {
                    Toast.makeText(requireContext(), "date required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String time = binding.time.getText().toString();

                if (time.isEmpty()) {
                    Toast.makeText(requireContext(), "time required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String taskId = String.valueOf(System.currentTimeMillis());

                Task task = new Task(taskId, title, date, time);

                firestore.collection("tasks")
                        .document(firebaseAuth.getCurrentUser().getUid())
                        .collection("myTasks")
                        .document(taskId)
                        .set(task)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(requireContext(), "Task inserted", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                String errorMessage = e.getLocalizedMessage();
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });


            }
        });


    }


}

















