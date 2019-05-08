package org.sluman.origami;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import androidx.annotation.VisibleForTesting;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class BaseActivity extends AppCompatActivity {
    private AnimatedVectorDrawableCompat shareMeAnim;
    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ImageView menuItem = (ImageView) menu.findItem(R.id.share);
        if (menuItem != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                shareMeAnim = AnimatedVectorDrawableCompat.create(this, R.drawable.avd);
                menuItem.setImageDrawable(shareMeAnim);
            } else {
                menuItem.setImageResource(R.drawable.ic_share);
            }
        }
        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Drawable drawable = item.getIcon();
                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
                }
                showShareDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    protected void showShareDialog() {
        int applicationNameId = getApplicationInfo().labelRes;
        final String appPackageName = getPackageName();
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, getString(applicationNameId));
        String text = getString(R.string.check_out);
        String link = "https://play.google.com/store/apps/details?id=" + appPackageName;
        i.putExtra(Intent.EXTRA_TEXT, text + " " + link);
        startActivity(Intent.createChooser(i, "Share:"));
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
