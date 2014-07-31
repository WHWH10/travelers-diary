package Helpers;

import android.location.Location;

public interface ILocationListener {
	public void locationFound(Location location);
	public void locationProviderUnavailable();
}
