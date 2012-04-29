package com.softskills.components;


import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class S3AutoComplete extends AutoCompleteTextView {

	private boolean show = false;
	private S3OnTextLengthChanged mOntextLengthChangedListener = null;
	
    public S3AutoComplete(Context context) {
        super(context);
    }

    public S3AutoComplete(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public S3AutoComplete(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }
    
    public void setShow (boolean show) {
    	this.show = show;
    }

    @Override
    public boolean enoughToFilter() {
        return show;
    }
    
    public void filter() {
    	performFiltering("", 0);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
            Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }
    
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
    	super.onSelectionChanged(selStart, selEnd);
    	
    	// After text is selected - clear the adapter
    	if (!isInEditMode())
    		emptyAdapter();
    }
    
    public void emptyAdapter() {
    	/*
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.autocomplete_item, 
        		new String[] {});
    	setAdapter(adapter);*/
    }
    
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    	if (mOntextLengthChangedListener != null && text != null)
    		mOntextLengthChangedListener.lengthChanged(text.length());
    	super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }
    
    public void setOnTextLengthChangedListener(S3OnTextLengthChanged listener) {
    	mOntextLengthChangedListener = listener;
    }
}
