package zhexian.app.smartcall.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import zhexian.app.smartcall.R;
import zhexian.app.smartcall.tools.Utils;

public class LetterSideBar extends View {
    int letterSize;
    ObjectAnimator _colorAnimation;
    private int itemHeight;
    private int itemSize;
    private int contentHeight;
    private Paint paint;
    private Bitmap letterBitmap;
    private List<Character> letters;
    private OnLetterChangedListener listener;
    private char currentGroupChar = '.';
    private Canvas mCanvas;
    private int mControlHalfWidth;
    private int controlWidth;

    public LetterSideBar(Context context) {
        super(context);
    }

    public LetterSideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LetterSideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void Init(List<Character> letters) {
        itemHeight = getResources().getDimensionPixelSize(R.dimen.navigator_text_height);
        itemSize = getResources().getDimensionPixelSize(R.dimen.navigator_text_size);
        this.letters = letters;
        contentHeight = (int) ((letters.size() + 0.5) * itemHeight);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = contentHeight;
        setLayoutParams(layoutParams);
        _colorAnimation = Utils.GenerateColorAnimator(getContext(), R.animator.letter_side_bar_bg_color, this);

        controlWidth = getResources().getDimensionPixelOffset(R.dimen.letter_side_bar_width);
        mControlHalfWidth = controlWidth / 2;

        Update();
        paint = new Paint();
        paint.setTextSize(itemSize);
        paint.setColor(getResources().getColor(R.color.gray_lighter));
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    public void Update() {
        letterBitmap = Bitmap.createBitmap(controlWidth, contentHeight,
                Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas();
        mCanvas.setBitmap(letterBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (letters == null) {
            return;
        }
        letterSize = letters.size();
        for (int i = 0; i < letterSize; i++) {
            String letter = String.valueOf(letters.get(i));
            mCanvas.drawText(letter, mControlHalfWidth - paint.measureText(letter) / 2,
                    itemHeight * i + itemHeight, paint);
        }
        if (letterBitmap != null) {
            canvas.drawBitmap(letterBitmap, 0, 0, paint);
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int actionName = event.getAction();

        if (actionName == MotionEvent.ACTION_DOWN || actionName == MotionEvent.ACTION_MOVE) {
            listener.OnTouchDown();
            touchDown((int) event.getY());

            if (actionName == MotionEvent.ACTION_DOWN)
                _colorAnimation.start();

            return true;
        }

        if (actionName == MotionEvent.ACTION_UP) {
            listener.OnTouchUp();
            _colorAnimation.reverse();
            return true;
        }

        return false;
    }

    private void touchDown(int y) {
        int position = (y / itemHeight);

        if (position < 0 || position >= letterSize)
            return;

        listener.OnTouchMove(y);
        char groupChar = letters.get(position);

        if (currentGroupChar == groupChar)
            return;

        listener.OnLetterChanged(groupChar, position);
        currentGroupChar = groupChar;
    }


    public void setOnLetterChangedListener(OnLetterChangedListener listener) {
        this.listener = listener;
    }


    public interface OnLetterChangedListener {
        void OnTouchDown();

        void OnTouchMove(int yPos);

        void OnTouchUp();

        void OnLetterChanged(Character s, int index);
    }

}
