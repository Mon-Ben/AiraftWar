package edu.hitsz.application;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.R;
import edu.hitsz.network.LeaderboardApiClient;
import edu.hitsz.network.LeaderboardEntry;

public class GlobalRankActivity extends AppCompatActivity {
    private TextView tvStatus;
    private GlobalRankAdapter adapter;
    private final LeaderboardApiClient apiClient = new LeaderboardApiClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_rank);

        tvStatus = findViewById(R.id.tv_global_rank_status);
        ListView listView = findViewById(R.id.lv_global_rank);
        adapter = new GlobalRankAdapter(this, new ArrayList<>());
        listView.setAdapter(adapter);

        Button btnBack = findViewById(R.id.btn_global_rank_back);
        btnBack.setOnClickListener(v -> finish());

        Button btnRefresh = findViewById(R.id.btn_global_rank_refresh);
        btnRefresh.setOnClickListener(v -> loadScores());

        loadScores();
    }

    private void loadScores() {
        tvStatus.setText("正在加载全球排行榜...");
        apiClient.fetchScores(new LeaderboardApiClient.FetchCallback() {
            @Override
            public void onSuccess(List<LeaderboardEntry> entries) {
                runOnUiThread(() -> {
                    adapter.updateData(entries);
                    tvStatus.setText(entries.isEmpty() ? "暂无全球排行榜记录" : "共 " + entries.size() + " 条记录");
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> tvStatus.setText("加载失败: " + message));
            }
        });
    }

    private static class GlobalRankAdapter extends BaseAdapter {
        private final LayoutInflater inflater;
        private List<LeaderboardEntry> data;

        GlobalRankAdapter(Context context, List<LeaderboardEntry> data) {
            this.inflater = LayoutInflater.from(context);
            this.data = data;
        }

        void updateData(List<LeaderboardEntry> newData) {
            this.data = newData;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_global_rank, parent, false);
                holder = new ViewHolder();
                holder.tvRank = convertView.findViewById(R.id.tv_global_rank);
                holder.tvName = convertView.findViewById(R.id.tv_global_name);
                holder.tvScore = convertView.findViewById(R.id.tv_global_score);
                holder.tvDifficulty = convertView.findViewById(R.id.tv_global_difficulty);
                holder.tvTime = convertView.findViewById(R.id.tv_global_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            LeaderboardEntry entry = data.get(position);
            holder.tvRank.setText(String.valueOf(position + 1));
            holder.tvName.setText(entry.getNickname());
            holder.tvScore.setText(String.valueOf(entry.getScore()));
            holder.tvDifficulty.setText(entry.getDifficulty());
            holder.tvTime.setText(entry.getFormattedTime());
            return convertView;
        }

        private static class ViewHolder {
            TextView tvRank;
            TextView tvName;
            TextView tvScore;
            TextView tvDifficulty;
            TextView tvTime;
        }
    }
}
