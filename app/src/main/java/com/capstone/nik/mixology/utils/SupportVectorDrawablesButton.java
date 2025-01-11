package com.capstone.nik.mixology.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.TextViewCompat;

import android.util.AttributeSet;

import com.capstone.nik.mixology.R;


/**
 * Created by nikhil on Nov 5
 */

public class SupportVectorDrawablesButton extends AppCompatButton {
    public SupportVectorDrawablesButton(Context context) {
        super(context);
    }

    public SupportVectorDrawablesButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSupportVectorDrawablesAttrs(attrs);
    }

    public SupportVectorDrawablesButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSupportVectorDrawablesAttrs(attrs);
    }

    /**
     * Loads the compound drawables natively on L+ devices and using AppCompat pre-L.
     * <p>
     * <i>Note:</i> If we ever need a TextView with compound drawables, this same technique is
     * applicable.
     */
    private void initSupportVectorDrawablesAttrs(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray attributeArray = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.SupportVectorDrawablesButton);

        Drawable drawableStart = null;
        Drawable drawableEnd = null;
        Drawable drawableTop = null;
        Drawable drawableBottom = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawableStart = attributeArray.getDrawable(
                    R.styleable.SupportVectorDrawablesButton_drawableStartCompat);
            drawableEnd = attributeArray.getDrawable(
                    R.styleable.SupportVectorDrawablesButton_drawableEndCompat);
            drawableTop = attributeArray.getDrawable(
                    R.styleable.SupportVectorDrawablesButton_drawableTopCompat);
            drawableBottom = attributeArray.getDrawable(
                    R.styleable.SupportVectorDrawablesButton_drawableBottomCompat);
        } else {
            int drawableStartId = attributeArray.getResourceId(
                    R.styleable.SupportVectorDrawablesButton_drawableStartCompat, -1);
            int drawableEndId = attributeArray.getResourceId(
                    R.styleable.SupportVectorDrawablesButton_drawableEndCompat, -1);
            int drawableTopId = attributeArray.getResourceId(
                    R.styleable.SupportVectorDrawablesButton_drawableTopCompat, -1);
            int drawableBottomId = attributeArray.getResourceId(
                    R.styleable.SupportVectorDrawablesButton_drawableBottomCompat, -1);

            if (drawableStartId != -1) {
                drawableStart = AppCompatResources.getDrawable(getContext(), drawableStartId);
            }
            if (drawableEndId != -1) {
                drawableEnd = AppCompatResources.getDrawable(getContext(), drawableEndId);
            }
            if (drawableTopId != -1) {
                drawableTop = AppCompatResources.getDrawable(getContext(), drawableTopId);
            }
            if (drawableBottomId != -1) {
                drawableBottom = AppCompatResources.getDrawable(getContext(), drawableBottomId);
            }
        }

        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                this, drawableStart, drawableTop, drawableEnd, drawableBottom);

        attributeArray.recycle();
    }
}