package com.example.portablegateway.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portablegateway.MyRecyclerViewAdapter;
import com.example.portablegateway.R;
import com.example.portablegateway.databinding.FragmentNotificationsBinding;

import java.util.List;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    List<String> dataReceived;
    MyRecyclerViewAdapter dataReceivedAdapter;
    private RecyclerView dataReceivedRv;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        dataReceived = notificationsViewModel.getDataReceived();
        init(root);
        return root;
    }
    public void init(View view){
        dataReceivedRv = view.findViewById(R.id.dataReceived);

        dataReceivedRv.setLayoutManager(new LinearLayoutManager(getActivity()));

        dataReceivedAdapter = new MyRecyclerViewAdapter(view.getContext(), dataReceived);

        dataReceivedRv.setAdapter(dataReceivedAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}