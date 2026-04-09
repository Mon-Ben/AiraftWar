package edu.hitsz.application;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import edu.hitsz.R;
import edu.hitsz.dao.FileScoreDao;
import edu.hitsz.dao.GameConfig;
import edu.hitsz.dao.ScoreDao;
import edu.hitsz.dao.ScoreItem;

public class RankActivity extends AppCompatActivity {

    private ListView listView;
    private ScoreDao scoreDao;
    private List<ScoreItem> scoreList;
    private RankAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        scoreDao = new FileScoreDao(this);
        refreshData();

        listView = findViewById(R.id.lv_rank);
        adapter = new RankAdapter(this, scoreList);
        listView.setAdapter(adapter);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        Button btnRestart = findViewById(R.id.btn_restart);
        btnRestart.setOnClickListener(v -> {
            // 返回到难度选择界面
            Intent intent = new Intent(RankActivity.this, DifficultyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void refreshData() {
        scoreList = scoreDao.listByDifficulty(GameConfig.difficulty);
        if (adapter != null) {
            adapter.updateData(scoreList);
        }
    }

    // 自定义 Adapter
    class RankAdapter extends BaseAdapter {
        private Context context;
        private List<ScoreItem> data;
        private LayoutInflater inflater;

        public RankAdapter(Context context, List<ScoreItem> data) {
            this.context = context;
            this.data = data;
            this.inflater = LayoutInflater.from(context);
        }

        public void updateData(List<ScoreItem> newData) {
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
                convertView = inflater.inflate(R.layout.list_item_rank, parent, false);
                holder = new ViewHolder();
                holder.tvRank = convertView.findViewById(R.id.tv_rank);
                holder.tvName = convertView.findViewById(R.id.tv_name);
                holder.tvScore = convertView.findViewById(R.id.tv_score);
                holder.tvTime = convertView.findViewById(R.id.tv_time);
                holder.btnDelete = convertView.findViewById(R.id.btn_delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ScoreItem item = data.get(position);
            holder.tvRank.setText(String.valueOf(position + 1));
            holder.tvName.setText(item.getPlayerName());
            holder.tvScore.setText(String.valueOf(item.getScore()));
            holder.tvTime.setText(item.getRankString()); // 使用 ScoreItem 中的格式化时间

            // 删除按钮点击事件
            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("删除记录")
                        .setMessage("确定删除第 " + (position + 1) + " 名的记录吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            scoreDao.deleteByRank(GameConfig.difficulty, position + 1);
                            refreshData(); // 刷新列表
                            Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });

            return convertView;
        }

        class ViewHolder {
            TextView tvRank, tvName, tvScore, tvTime;
            Button btnDelete;
        }
    }
}