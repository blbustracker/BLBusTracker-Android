package org.unibl.etf.blbustracker.navigationtabs.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.unibl.etf.blbustracker.BuildConfig;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.utils.ToolbarUtil;

public class AboutFragment extends Fragment
{
    private static final String DOUBLE_LINE_BREAK = "\n\n";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        String title = getString(R.string.about);
        ToolbarUtil.initToolbar(getActivity(), view, title);
        initText(view);


        return view;
    }

    private void initText(View view)
    {
        TextView textView = (TextView) view.findViewById(R.id.about_content);
        String content = "";
        content += getString(R.string.app_name) + DOUBLE_LINE_BREAK;
        content += getString(R.string.about_paragraph_1) + DOUBLE_LINE_BREAK;
        content += getString(R.string.about_paragraph_2) + DOUBLE_LINE_BREAK;
        content += getString(R.string.version) + " " + BuildConfig.VERSION_NAME + DOUBLE_LINE_BREAK;
        content += getString(R.string.undp_header);

        textView.setText(content);
    }
}