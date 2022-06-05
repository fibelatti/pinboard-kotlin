package com.fibelatti.core.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

/***
 * A default implementation of {@link ResourceProvider} to retrieve Android resources from a {@link Context}.
 *
 * This class also registers a {@link BroadcastReceiver} to handle locale changes on its own, ensuring that the
 * provided resources are correctly localized.
 *
 * This class is implemented in Java because Kotlin is not properly translating `varargs formatArgs: Any` when invoking
 * `getString(resId, formatArgs)`.
 */
public class AppResourceProvider implements ResourceProvider {

    private static final String TAG = "AppResourceProvider";

    @NonNull
    private Context context;

    @NonNull
    @SuppressWarnings("FieldCanBeLocal")
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context ctx, Intent intent) {
            context = getLocalizedContext(ctx);
        }
    };

    public AppResourceProvider(@NonNull Context context) {
        this.context = getLocalizedContext(context);

        context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
    }

    @NonNull
    private Context getLocalizedContext(@NonNull Context context) {
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(Locale.getDefault());
        return context.createConfigurationContext(config);
    }

    @NonNull
    @Override
    public String getString(int resId) {
        return context.getString(resId);
    }

    @NonNull
    @Override
    public String getString(int resId, @NonNull Object... formatArgs) {
        return context.getString(resId, formatArgs);
    }

    @Nullable
    @Override
    public String getJsonFromAssets(@NonNull String fileName) {
        try (InputStreamReader reader = new InputStreamReader(context.getAssets().open(fileName))) {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(reader);
            String read = bufferedReader.readLine();

            while (read != null) {
                stringBuilder.append(read);
                read = bufferedReader.readLine();
            }

            return stringBuilder.toString();
        } catch (Exception exception) {
            Log.d(TAG, TAG + ".getJsonFromAssets", exception);
        }

        return null;
    }
}
