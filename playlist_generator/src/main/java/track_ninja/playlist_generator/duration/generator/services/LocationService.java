package track_ninja.playlist_generator.duration.generator.services;

public interface LocationService {

    double getTravelDuration(String startPoint, String endPoint) throws IllegalArgumentException;
}
