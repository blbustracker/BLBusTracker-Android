package org.unibl.etf.blbustracker.navigationtabs.about;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.utils.ToolbarUtil;

public class AboutFragment extends Fragment
{
    private static final String LINE_BREAK = "\n\n";

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
        content += getString(R.string.app_name) + LINE_BREAK;
        content += getString(R.string.about_paragraph_1) + LINE_BREAK;
        content += getString(R.string.about_paragraph_2) + LINE_BREAK;
        content += getString(R.string.version) + LINE_BREAK;
        content += getString(R.string.undp_header);

        textView.setText(content);
    }
}