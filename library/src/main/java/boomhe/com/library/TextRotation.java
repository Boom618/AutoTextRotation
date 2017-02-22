package boomhe.com.library;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

/**
 * Created by ${Hedong} on 17/2/21.
 * 文字轮播
 */

public class TextRotation extends TextSwitcher implements ViewSwitcher.ViewFactory {

    private static final int START_TEXT = 0; // 开始轮播
    private static final int STOP_TEXT = 1;   // 停止轮播

    private float mTextSize = 16;   // 文字大小
    private int mPadding = 5;       // 文字边距
    private int mTextColor = Color.RED; // 文字颜色
    private int currentId = -1;         // 获取 position 的文字

    private Context mContext;
    private Handler mHandler;
    private ArrayList<String> mTextList = new ArrayList<>();
    public OnItemClickListener onItemClickListener;

    // 轮播文本点击监听器
    public interface OnItemClickListener {
        // 点击回调
        void onItemClick(int position);
    }

    // 设置点击监听
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public TextRotation(Context context) {
        this(context, null);
        mContext = context;
    }

    public TextRotation(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

    }

    /**
     * @param mTextSize  文字大小
     * @param mPadding   边距
     * @param mTextColor 颜色
     */
    public void setText(float mTextSize, int mPadding, int mTextColor) {
        mTextSize = mTextSize;
        mPadding = mPadding;
        mTextColor = mTextColor;

    }

    // 设置动画
    public void setTextAnim(long time) {
        setFactory(this);
        TranslateAnimation in = new TranslateAnimation(0, 0, time, 0);
        in.setDuration(time);
        in.setInterpolator(new AccelerateInterpolator());
        TranslateAnimation out = new TranslateAnimation(0, 0, 0, -time);
        out.setDuration(time);
        out.setInterpolator(new AccelerateInterpolator());
        setInAnimation(in);
        setOutAnimation(out);

    }

    // 设置数据源
    public void setTextData(ArrayList<String> textList) {
        mTextList.clear();
        mTextList.addAll(textList);
        currentId = -1;

    }

    // 设置间隔
    public void setTextTime(final long time) {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case START_TEXT:
                        if (mTextList.size() > 0) {
                            currentId++;
                            setText(mTextList.get(currentId % mTextList.size()));
                        }
                        mHandler.sendEmptyMessageDelayed(START_TEXT, time);
                        break;
                    case STOP_TEXT:
                        mHandler.removeMessages(STOP_TEXT);
                        break;
                }
            }
        };

    }

    /**
     * 开始滚动
     */
    public void startAutoScroll() {
        mHandler.sendEmptyMessage(START_TEXT);
    }

    /**
     * 停止滚动
     */
    public void stopAutoScroll() {
        mHandler.sendEmptyMessage(STOP_TEXT);
    }


    @Override
    public View makeView() {
        TextView t = new TextView(mContext);
        t.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        t.setMaxLines(1);
        t.setPadding(mPadding, mPadding, mPadding, mPadding);
        t.setTextColor(mTextColor);
        t.setTextSize(mTextSize);

        t.setClickable(true);
        t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null && mTextList.size() > 0 && currentId != -1) {
                    onItemClickListener.onItemClick(currentId % mTextList.size());
                }
            }
        });
        return t;
    }
}
