import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Base64.NO_WRAP
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun getHashKey(context: Context): String? {
    try {
        if (Build.VERSION.SDK_INT >= 28) {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName,PackageManager.GET_SIGNING_CERTIFICATES)
            val signatures = packageInfo.signingInfo.apkContentsSigners
            val md = MessageDigest.getInstance("SHA")
            for (signature in signatures) {
                md.update(signature.toByteArray())
                return String(Base64.encode(md.digest(), NO_WRAP))
            }
        } else {
            val packageInfo =
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES) ?: return null

            for (signature in packageInfo!!.signatures) {
                try {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                } catch (e: NoSuchAlgorithmException) {
                    // ERROR LOG
                }
            }
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }

    return null
}