package com.bts.centered_icon_button;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatButton;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CenteredIconButton extends AppCompatButton {

    private static final String DELIMITERS = "\n";
    private static final int DRAWABLE_START_POSITION = 0;
    private static final int DRAWABLE_TOP_POSITION = 1;
    private static final int DRAWABLE_END_POSITION = 2;
    private static final int DRAWABLE_BOTTOM_POSITION = 3;
    private static final int DRAWABLES_LENGTH = 4;

    private Rect textBoundsRect;
    @ColorInt
    private int mTintColor = Color.TRANSPARENT;
    private int mLeftPadding;
    private int mRightPadding;

    public CenteredIconButton(Context context) {
        super(context);
        init(context, null);
    }

    public CenteredIconButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CenteredIconButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            @SuppressLint("CustomViewStyleable")
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CenteredIconButton);

            mTintColor = typedArray.getColor(R.styleable.CenteredIconButton_cib_drawableTint, Color.TRANSPARENT);

            float defaultDrawablePadding = getResources().getDimension(R.dimen.def_drawable_padding);
            int drawablePadding = (int) typedArray.getDimension(R.styleable.CenteredIconButton_cib_drawablePadding, defaultDrawablePadding);
            setCompoundDrawablePadding(drawablePadding);

            updateTint();
            typedArray.recycle();
        }
        mLeftPadding = getPaddingLeft();
        mRightPadding = getPaddingRight();
    }

    private void updateTint() {
        if (mTintColor != Color.TRANSPARENT) {
            Drawable[] drawables = getCompoundDrawablesRelative();
            if (drawables.length != DRAWABLES_LENGTH) return;

            Drawable[] wrappedDrawables = new Drawable[DRAWABLES_LENGTH];
            for (int i = 0; i < DRAWABLES_LENGTH; i++) {
                Drawable drawable = drawables[i];
                if (drawable != null) {
                    Drawable wrappedDrawable = DrawableCompat.wrap(drawable).mutate();
                    DrawableCompat.setTint(wrappedDrawable, mTintColor);
                    wrappedDrawables[i] = wrappedDrawable;
                }
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(wrappedDrawables[DRAWABLE_START_POSITION],
                    wrappedDrawables[DRAWABLE_TOP_POSITION],
                    wrappedDrawables[DRAWABLE_END_POSITION],
                    wrappedDrawables[DRAWABLE_BOTTOM_POSITION]);
        }
    }

    @Override
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(@DrawableRes int start, @DrawableRes int top, @DrawableRes int end, @DrawableRes int bottom) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
        updatePadding();
    }

    @Override
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
        updatePadding();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        updatePadding();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updatePadding(w);
    }

    private void updatePadding() {
        updatePadding(getMeasuredWidth());
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        mLeftPadding = left;
        mRightPadding = right;
        updatePadding();
    }

    private void updatePadding(int width) {
        if (width == 0) return;

        Drawable[] compoundDrawables = getCompoundDrawablesRelative();
        if (compoundDrawables.length != DRAWABLES_LENGTH) return;

        Drawable leftDrawable = compoundDrawables[DRAWABLE_START_POSITION];
        Drawable rightDrawable = compoundDrawables[DRAWABLE_END_POSITION];
        if (leftDrawable == null && rightDrawable == null) return;

        int textWidth = getTextWidth();
        int iconPadding = Math.max(getCompoundDrawablePadding(), 1);
        int paddingSize;

        if (leftDrawable != null && rightDrawable != null) {
            paddingSize = (width - leftDrawable.getIntrinsicWidth() - rightDrawable.getIntrinsicWidth() - textWidth - iconPadding * 4) / 2;
        } else if (leftDrawable != null) {
            paddingSize = (width - leftDrawable.getIntrinsicWidth() - iconPadding * 2 - textWidth) / 2;
        } else {
            paddingSize = (width - rightDrawable.getIntrinsicWidth() - iconPadding * 2 - textWidth) / 2;
        }
        super.setPadding(Math.max(mLeftPadding, paddingSize), getPaddingTop(), Math.max(paddingSize, mRightPadding), getPaddingBottom());
    }

    private int getTextWidth() {
        if (textBoundsRect == null) {
            textBoundsRect = new Rect();
        }
        Paint paint = getPaint();
        String text = divideText();
        paint.getTextBounds(text, 0, text.length(), textBoundsRect);
        return textBoundsRect.width();
    }

    private String divideText() {
        String text = getText().toString();
        if (text.isEmpty()) {
            return "";
        }
        List<String> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(text, DELIMITERS, false);
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        if (list.size() == 1) {
            return isAllCaps() ? list.get(0).toUpperCase() : list.get(0);
        }
        String longPart = list.get(0);
        for(int i = 0; i < list.size() - 1; i++) {
            if (list.get(i + 1).length() > list.get(i).length()) {
                longPart = list.get(i + 1);
            }
        }

        return isAllCaps() ? longPart.toUpperCase() : longPart;
    }

    public boolean isAllCaps() {
        TransformationMethod method = getTransformationMethod();
        if(method == null) return false;

        return method.getClass().getSimpleName().equals("AllCapsTransformationMethod");
    }
}
