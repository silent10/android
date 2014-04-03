package com.virtual_hotel_agent.search.controllers.web_services;

import java.util.List;

import org.joda.time.LocalDate;

import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.exception.UrlRedirectionException;
import com.ean.mobile.hotel.HotelRoom;
import com.ean.mobile.hotel.request.RoomAvailabilityRequest;
import com.ean.mobile.request.RequestProcessor;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface.DownloaderStatus;

public class RoomsUpdaterTask extends DownloaderTask {
	private static final String TAG = "RoomUpdaterTask";

	public final long hotelId;
    private final LocalDate arrivalDate;
    private final LocalDate departureDate;
    public EanWsError eanWsError;

    public RoomsUpdaterTask(final long hotelId,
            final LocalDate arrivalDate, final LocalDate departureDate) {
    	super(-1);
        this.hotelId = hotelId;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
    }
    
    public RoomsUpdaterTask(final long hotelId) {
    	super(-1);
        this.hotelId = hotelId;
        this.arrivalDate = VHAApplication.arrivalDate;
        this.departureDate = VHAApplication.departureDate;
    }

	
	@Override
	protected Object doInBackground(Void... params) {
//		JSONObject result= XpediaProtocolStatic.getRoomInformationForHotel(mContext, mHotelData.mSummary.mHotelId,
//				MyApplication.getExpediaAppState(),
//				SettingsAPI.getCurrencyCode(mContext));
//
//		mHotelData.mSummary.updateRoomDetails(result);
		
		 try {
			 eanWsError = null;
             final RoomAvailabilityRequest request
                 = new RoomAvailabilityRequest(hotelId, VHAApplication.occupancy(), arrivalDate, departureDate);
             List<HotelRoom> hotelRooms = RequestProcessor.run(request);
             VHAApplication.HOTEL_ROOMS.put(hotelId, hotelRooms);
             mProgress = DownloaderStatus.Finished;
             return hotelRooms;
         } catch (EanWsError ewe) {
             VHAApplication.logError(TAG, "An error occurred in the api", ewe);
             eanWsError = ewe;
             mProgress = DownloaderStatus.FinishedWithError;
         } catch (UrlRedirectionException ure) {
             VHAApplication.sendRedirectionToast();
             mProgress = DownloaderStatus.FinishedWithError;
         }
         return null;
	}

}
