package org.unibl.etf.blbustracker.navigationtabs.reportproblem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Keep;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.networkmanager.NetworkStatus;
import org.unibl.etf.blbustracker.utils.AlertUtil;
import org.unibl.etf.blbustracker.utils.ToolbarUtil;

public class ReportProblemFragment extends Fragment
{
    private EditText reportTitle;
    private TextInputEditText reportContent;

    private ReportProblemModel reportProblemModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_report_problem, container, false);

        String title = getString(R.string.report_problem);
        ToolbarUtil.initToolbar(getActivity(), view, title);

        reportTitle = (EditText) view.findViewById(R.id.report_title);
        reportContent = (TextInputEditText) view.findViewById(R.id.report_content);

        TextInputLayout asd = view.findViewById(R.id.report_content_container);
        asd.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

        initSendRpoertButton(view);
        initClearButton(view);

        return view;
    }

    private void initSendRpoertButton(View view)
    {
        Button sendReportBtn = (Button) view.findViewById(R.id.send_button);
        sendReportBtn.setOnClickListener(v ->
        {
            String title = reportTitle.getText().toString().trim();
            String message = reportContent.getText().toString().trim();

            if ("".equals(title) || "".equals(message))
                AlertUtil.showWarningAlert(getContext(), getString(R.string.fill_required_fields));
            else
            {
                if (!NetworkStatus.isNetworkAvailable(getContext()))
                    AlertUtil.showWarningAlert(getContext(), getString(R.string.no_internet));
                else
                    AlertUtil.showAlertDialog(getContext(), getString(R.string.are_you_sure), (dialog, which) ->
                    {
                        if (reportProblemModel == null)
                            reportProblemModel = new ReportProblemModel(getContext());

                        reportProblemModel.setPOSTBody(title, message);
                        reportProblemModel.startListening();
                        clearFieldContent();
                    },null);
            }

        });
    }

    private void initClearButton(View view)
    {
        Button clear = (Button) view.findViewById(R.id.clear_button);
        clear.setOnClickListener(v -> clearFieldContent());
    }

    private void clearFieldContent()
    {
        reportTitle.getText().clear();
        reportContent.getText().clear();
        reportContent.clearFocus();
        reportTitle.clearFocus();
    }

    @Override
    public void onStop()
    {
        if (reportProblemModel != null)
            reportProblemModel.stopListening();
        reportProblemModel = null;
        super.onStop();
    }

    @Keep
    public ReportProblemFragment()
    {
    }
}