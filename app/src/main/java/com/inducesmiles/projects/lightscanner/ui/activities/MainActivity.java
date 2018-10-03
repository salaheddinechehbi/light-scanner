package com.inducesmiles.projects.lightscanner.ui.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.inducesmiles.projects.lightscanner.LightScannerApplication;
import com.inducesmiles.projects.lightscanner.R;
import com.inducesmiles.projects.lightscanner.async.FileLoadCallback;
import com.inducesmiles.projects.lightscanner.models.CroppedModel;
import com.inducesmiles.projects.lightscanner.models.LocalPdf;
import com.inducesmiles.projects.lightscanner.persistence.RepoManager;
import com.inducesmiles.projects.lightscanner.ui.adapters.PdfListAdapter;
import com.inducesmiles.projects.lightscanner.ui.base.BaseActivity;
import com.inducesmiles.projects.lightscanner.utils.L;
import com.inducesmiles.projects.lightscanner.utils.Util;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    RelativeLayout cameraLayout;
    RelativeLayout galleryLayout;

    public static final int PHOTO_PICKER = 12;

    @BindView(R.id.pw_main)
    ProgressWheel progressWheel;
    @BindView(R.id.rv_pdfs_main)
    RecyclerView mRecyclerView;
    @BindView(R.id.no_file_layout_main)
    LinearLayout noFileLayout;

    private List<LocalPdf> localPdfs = new ArrayList<>();
    private PdfListAdapter pdfListAdapter;
    private Dialog renameFileDialog;
    private LocalPdf toRenamePdf;
    private EditText renameFileEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        pdfListAdapter = new PdfListAdapter(this, localPdfs);
        pdfListAdapter.setiInteractionListener(iInteractionListener);
        mRecyclerView.setAdapter(pdfListAdapter);

        createRenameFileDialog();
        loadPdfs();
    }

    private void loadPdfs() {

        progressWheel.setVisibility(View.VISIBLE);

        LightScannerApplication
                .getApplication()
                .getExecutorService()
                .execute(new LoadPdfsTask(loadCallback));
    }

    private void createRenameFileDialog() {

        renameFileDialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_rename_file, null);
        renameFileEditText = view.findViewById(R.id.edt_rename_file_dialog);
        view.findViewById(R.id.btn_rename).setOnClickListener(clickListener);
        view.findViewById(R.id.btn_cancel_rename).setOnClickListener(clickListener);

        renameFileDialog.setContentView(view);

    }

    private FileLoadCallback loadCallback = new FileLoadCallback() {
        @Override
        public void done(List<LocalPdf> list) {

            localPdfs.clear();
            localPdfs.addAll(list);
            progressWheel.setVisibility(View.GONE);

            if (pdfListAdapter != null)
                pdfListAdapter.notifyDataSetChanged();

            if (localPdfs.size() <= 0) {
                noFileLayout.setVisibility(View.VISIBLE);
            }else {
                noFileLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public void error(Throwable throwable) {

        }
    };

    public static void start(Context photoPreviewActivity) {

        Intent intent = new Intent(photoPreviewActivity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        photoPreviewActivity.startActivity(intent);
    }

    private class LoadPdfsTask implements Runnable {

        Handler handler = new Handler(Looper.getMainLooper());
        List<LocalPdf> result = new ArrayList<>();
        FileLoadCallback fileLoadCallback;


        public LoadPdfsTask(FileLoadCallback callback) {
            fileLoadCallback = callback;
        }


        @Override
        public void run() {

            try {

                result = RepoManager.manager().getDatabaseManager().localPdfDao().all();
            }catch (Exception e) {
                L.wtf(e);
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (fileLoadCallback != null)
                        fileLoadCallback.done(result);
                }
            });
        }

    }

    Bitmap getThumb(File file) throws Exception {
        PDDocument document = PDDocument.load(new FileInputStream(file));
        // Create a renderer for the document
        PDFRenderer renderer = new PDFRenderer(document);
        // Render the image to an RGB Bitmap
        return renderer.renderImage(0, 1, Bitmap.Config.RGB_565);
    }

    @OnClick(R.id.fab_create_pdf_main) public void onCreatePDFClick() {

        Dialog dialog = new Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_option_dialog, null);

        cameraLayout = view.findViewById(R.id.layout_camera_option_dialog);
        galleryLayout = view.findViewById(R.id.layout_gallery_option_dialog);

        cameraLayout.setOnClickListener(clickListener);
        galleryLayout.setOnClickListener(clickListener);

        dialog.setContentView(view);
        dialog.show();

    }

    private void startImagePicker() {

        ImagePicker.create(this)
                .folderMode(true)
                .toolbarImageTitle("Select Images")
                .toolbarFolderTitle("Select Images")
                .multi()
                .theme(R.style.ImagePickerTheme)
                .start(PHOTO_PICKER);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.layout_camera_option_dialog:
                    startActivity(new Intent(MainActivity.this, CameraActivity.class));
                    break;
                case R.id.layout_gallery_option_dialog:
                    startImagePicker();
                    break;
                case R.id.btn_rename:

                    String name = Util.textOf(renameFileEditText);
                    if (!name.isEmpty()) {

                        File file = new File(toRenamePdf.path);
                        File newFile = new File(file, name + ".pdf");
                        file.renameTo(newFile);

                        toRenamePdf.path = newFile.getAbsolutePath();
                        int idx = localPdfs.indexOf(toRenamePdf);
                        L.fine(idx + "");

                        if (pdfListAdapter != null)
                            pdfListAdapter.notifyDataSetChanged();
                        toast("File renamed!");
                    }

                    renameFileDialog.cancel();
                    break;
                case R.id.btn_cancel_rename:
                    renameFileDialog.cancel();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            List<CroppedModel> croppedModels = new ArrayList<>();
            switch (requestCode) {

                case PHOTO_PICKER : {
                    List<Image> images = ImagePicker.getImages(data);
                    for (Image image : images) {
                        CroppedModel model = new CroppedModel();
                        model.path = image.getPath();
                        model.selected = false;
                        croppedModels.add(model);
                    }

                    Intent intent = new Intent(this, ImageCropperActivity.class);
                    intent.putExtra(CroppedModel.KEY, Parcels.wrap(croppedModels));
                    startActivity(intent);
                }
            }
        }
    }

    private PdfListAdapter.IInteractionListener iInteractionListener = new PdfListAdapter.IInteractionListener() {
        @Override
        public void onRename(LocalPdf toRename) {

            toRenamePdf = toRename;
            renameFileDialog.show();
        }

        @Override
        public void onDelete(final LocalPdf toDelete) {

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete " + toDelete.name)
                    .setMessage("Are you sure you want to delete this PDF file? You will never be able to retrieve it.")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            File file = new File(toDelete.path);
                            boolean deleted = file.delete();
                            if (!deleted) {
                                toast("Failed to delete file. Please retry");
                                return;
                            }

                            int idx = localPdfs.indexOf(toDelete);
                            localPdfs.remove(idx);
                            if (pdfListAdapter != null)
                                pdfListAdapter.notifyDataSetChanged();

                            RepoManager
                                    .manager()
                                    .getDatabaseManager()
                                    .localPdfDao()
                                    .remove(toDelete.path);

                            toast("File Deleted!");
                        }
                    })
                    .setNegativeButton("NO", null)
                    .create()
                    .show();
        }

        @Override
        public void onShare(LocalPdf toShare) {


        }

        @Override
        public void onView(LocalPdf onView) {
            viewPdf(onView);
        }
    };

    private void viewPdf(LocalPdf localPdf){
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setDataAndType(Uri.fromFile(new File(localPdf.path)), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private void emailNote(LocalPdf localPdf)
    {
        Intent email = new Intent(Intent.ACTION_SEND);
        Uri uri = Uri.parse(localPdf.path);
        email.putExtra(Intent.EXTRA_STREAM, uri);
        email.setType("message/rfc822");
        startActivity(email);
    }

}
