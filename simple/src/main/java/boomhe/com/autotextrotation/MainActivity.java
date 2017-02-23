package boomhe.com.autotextrotation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;

import boomhe.com.library.TextRotation;
import boomhe.com.library.ZoomImageView;

public class MainActivity extends AppCompatActivity {

    private TextRotation TextView;
    private ZoomImageView imageView;
    private ArrayList<String> titleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        imageView = (ZoomImageView) findViewById(R.id.image);
        imageView.setImageResource(R.mipmap.image1);
        TextView = (TextRotation) findViewById(R.id.text);
        titleList.add("你是天上最受宠的一架钢琴");
        titleList.add("我是丑人脸上的鼻涕");
        titleList.add("你发出完美的声音");
        titleList.add("我被默默揩去");
        titleList.add("你冷酷外表下藏着诗情画意");
        titleList.add("我已经够胖还吃东西");
        titleList.add("你踏着七彩祥云离去");
        titleList.add("我被留在这里");
        TextView.setTextData(titleList);
        TextView.setText(26, 5, Color.RED);//设置属性
        TextView.setTextTime(3000);//设置停留时长间隔
        TextView.setTextAnim(300);//设置进入和退出的时间间隔
        TextView.setOnItemClickListener(new TextRotation.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(MainActivity.this, "点击了 : " + titleList.get(position), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView.startAutoScroll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        TextView.stopAutoScroll();
    }
}
