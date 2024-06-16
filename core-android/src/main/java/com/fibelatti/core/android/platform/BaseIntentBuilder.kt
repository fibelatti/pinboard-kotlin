package com.fibelatti.core.android.platform

import android.content.Context
import android.content.Intent

public open class BaseIntentBuilder(context: Context?, clazz: Class<*>) {

    protected val intent: Intent = Intent(context, clazz)

    public fun clearTop(): BaseIntentBuilder = apply { intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }

    public fun build(): Intent = intent
}
