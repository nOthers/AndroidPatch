package android.content.pm;

public interface IPackageManager {
    PackageInfo getPackageInfo(String packageName, int flags, int userId);
}
