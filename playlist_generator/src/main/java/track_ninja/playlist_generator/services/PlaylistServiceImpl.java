package track_ninja.playlist_generator.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import track_ninja.playlist_generator.exceptions.GenreDoesNotExistException;
import track_ninja.playlist_generator.exceptions.UserNotFoundException;
import track_ninja.playlist_generator.models.Playlist;
import track_ninja.playlist_generator.exceptions.NoGeneratedPlaylistsException;
import track_ninja.playlist_generator.models.dtos.PlayListEditDTO;
import track_ninja.playlist_generator.models.dtos.PlaylistDTO;
import track_ninja.playlist_generator.models.mappers.ModelMapper;
import track_ninja.playlist_generator.repositories.GenreRepository;
import track_ninja.playlist_generator.repositories.PlaylistRepository;
import track_ninja.playlist_generator.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class PlaylistServiceImpl implements PlaylistService{
    private static final String NO_PLAYLISTS_GENERATED_ERROR_MESSAGE = "No playlists generated!";
    private static final String NO_PLAYLISTS_GENERATED_BY_THIS_USER_ERROR_MESSAGE = "No playlists generated by this user!";
    private static final String NO_PLAYLISTS_GENERATED_WITH_SUCH_TITLE_ERROR_MESSAGE = "No playlists generated with such title!";
    private static final String NO_PLAYLISTS_GENERATED_FOR_THIS_GENRE_ERROR_MESSAGE = "No playlists generated for this genre!";
    private static final String NO_PLAYLISTS_WITH_DURATION_WITHIN_THIS_RANGE_ERROR_MESSAGE = "No playlists with duration within this range!";

    private static final String RETRIEVING_ALL_PLAYLISTS_MESSAGE = "Retrieving all playlists...";
    private static final String RETRIEVED_PLAYLISTS_MESSAGE = "Retrieved playlists!";
    private static final String RETRIEVING_ALL_PLAYLISTS_FOR_GENRE_MESSAGE = "Retrieving all playlists for genre %s...";
    private static final String COULD_NOT_RETRIEVE_PLAYLISTS_MESSAGE = "Could not retrieve playlists! %s";
    private static final String RETRIEVING_ALL_PLAYLISTS_FOR_USER_MESSAGE = "Retrieving all playlists for user %s...";
    private static final String RETRIEVING_ALL_PLAYLISTS_FOR_TITLE_MESSAGE = "Retrieving all playlists for title like %s...";
    private static final String RETRIEVING_ALL_PLAYLISTS_FOR_DURATION_MESSAGE = "Retrieving all playlists for duration around %d minutes...";

    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);
    private static final String LOOKING_FOR_PLAYLIST_WITH_ID_MESSAGE = "Looking for playlist with id %d...";
    private static final String NO_PLAYLIST_WITH_THIS_ID_ERROR_MESSAGE = "No playlist with this id!";
    private static final String PLAYLIST_FOUND_MESSAGE = "Playlist found!";
    private static final String PLAYLIST_EDITED_MESSAGE = "Playlist edited!";
    private static final String PLAYLIST_DELETED_MESSAGE = "Playlist deleted!";

    private PlaylistRepository playlistRepository;
    private GenreRepository genreRepository;
    private UserRepository userRepository;

    @Autowired
    public PlaylistServiceImpl(PlaylistRepository playlistRepository, GenreRepository genreRepository, UserRepository userRepository) {
        this.playlistRepository = playlistRepository;
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
    }


    @Override
    public List<PlaylistDTO> getAll() {
        logger.info(RETRIEVING_ALL_PLAYLISTS_MESSAGE);
        List<Playlist> playlists = playlistRepository.findAllByIsDeletedFalse();
        if (playlists.isEmpty()) {
            handleNoGeneratedPlaylistsException(NO_PLAYLISTS_GENERATED_ERROR_MESSAGE);
        }
        logger.info(RETRIEVED_PLAYLISTS_MESSAGE);
        return mapListOfPlaylistToDTOs(playlists);
    }

    @Override
    public List<PlaylistDTO> getByGenre(String genre) {
        logger.info(String.format(RETRIEVING_ALL_PLAYLISTS_FOR_GENRE_MESSAGE, genre));
        if (!genreRepository.existsByName(genre)) {
            GenreDoesNotExistException gdne = new GenreDoesNotExistException();
            logger.error(String.format(COULD_NOT_RETRIEVE_PLAYLISTS_MESSAGE, gdne.getMessage()));
            throw gdne;
        }
        List<Playlist> playlists = playlistRepository.findPlaylistsByIsDeletedFalseAndGenresContaining_Name(genre);
        if (playlists.isEmpty()) {
            handleNoGeneratedPlaylistsException(NO_PLAYLISTS_GENERATED_FOR_THIS_GENRE_ERROR_MESSAGE);
        }
        logger.info(RETRIEVED_PLAYLISTS_MESSAGE);
        return mapListOfPlaylistToDTOs(playlists);
    }

    @Override
    public List<PlaylistDTO> getByUser(String username) {
        logger.info(String.format(RETRIEVING_ALL_PLAYLISTS_FOR_USER_MESSAGE, username));
        if (!userRepository.existsByUsername(username)) {
            UserNotFoundException unf = new UserNotFoundException();
            logger.error(String.format(COULD_NOT_RETRIEVE_PLAYLISTS_MESSAGE, unf.getMessage()));
            throw unf;
        }
        List<Playlist> playlists = playlistRepository.findAllByIsDeletedFalseAndUser_User_Username(username);
        if (playlists.isEmpty()) {
            handleNoGeneratedPlaylistsException(NO_PLAYLISTS_GENERATED_BY_THIS_USER_ERROR_MESSAGE);
        }
        logger.info(RETRIEVED_PLAYLISTS_MESSAGE);
        return mapListOfPlaylistToDTOs(playlists);
    }

    @Override
    public List<PlaylistDTO> getByTitle(String title) {
        logger.info(String.format(RETRIEVING_ALL_PLAYLISTS_FOR_TITLE_MESSAGE, title));
        List<Playlist> playlists = playlistRepository.findAllByIsDeletedFalseAndTitleLike("%" + title + "%");
        if (playlists.isEmpty()) {
            handleNoGeneratedPlaylistsException(NO_PLAYLISTS_GENERATED_WITH_SUCH_TITLE_ERROR_MESSAGE);
        }
        logger.info(RETRIEVED_PLAYLISTS_MESSAGE);
        return mapListOfPlaylistToDTOs(playlists);
    }

    @Override
    public List<PlaylistDTO> getByDuration(long durationMinutes) {
        logger.info(String.format(RETRIEVING_ALL_PLAYLISTS_FOR_DURATION_MESSAGE, durationMinutes));
        long durationSeconds = durationMinutes * 60;
        List<Playlist> playlists = playlistRepository.findAllByIsDeletedFalseAndDurationBetween(durationSeconds - 600, durationSeconds + 600);
        if (playlists.isEmpty()) {
            handleNoGeneratedPlaylistsException(NO_PLAYLISTS_WITH_DURATION_WITHIN_THIS_RANGE_ERROR_MESSAGE);
        }
        logger.info(RETRIEVED_PLAYLISTS_MESSAGE);
        return mapListOfPlaylistToDTOs(playlists);
    }

    @Override
    public boolean playlistsExist() {
        return !getAll().isEmpty();
    }

    @Override
    public PlaylistDTO getById(int id) {
        logger.info(String.format(LOOKING_FOR_PLAYLIST_WITH_ID_MESSAGE, id));
        Playlist playlist = playlistRepository.findByIsDeletedFalseAndPlaylistId(id);
        if (playlist == null) {
            handleNoGeneratedPlaylistsException(NO_PLAYLIST_WITH_THIS_ID_ERROR_MESSAGE);
        }
        logger.info(PLAYLIST_FOUND_MESSAGE);
        return ModelMapper.playlistToDTO(playlist);
    }

    @Override
    public boolean editPlaylist(PlayListEditDTO playListEditDTO) {
        logger.info(String.format("Editing playlist with id %d...", playListEditDTO.getPlaylistId()));
        Playlist playlist = playlistRepository.findByIsDeletedFalseAndPlaylistId(playListEditDTO.getPlaylistId());
        if (playlist == null) {
            handleNoGeneratedPlaylistsException(NO_PLAYLIST_WITH_THIS_ID_ERROR_MESSAGE);
        }
        playlist.setTitle(playListEditDTO.getTitle());
        playlistRepository.save(playlist);
        logger.info(PLAYLIST_EDITED_MESSAGE);
        return true;
    }

    @Override
    public boolean deletePlaylist(int id) {
        logger.info(String.format("Deleting playlist with id %d...", id));
        Playlist playlist = playlistRepository.findByIsDeletedFalseAndPlaylistId(id);
        if (playlist == null) {
            handleNoGeneratedPlaylistsException(NO_PLAYLIST_WITH_THIS_ID_ERROR_MESSAGE);
        }
        playlist.setDeleted(true);
        playlistRepository.save(playlist);
        logger.info(PLAYLIST_DELETED_MESSAGE);
        return false;
    }

    private void handleNoGeneratedPlaylistsException(String noPlaylistsGeneratedErrorMessage) {
        NoGeneratedPlaylistsException ngp = new NoGeneratedPlaylistsException(noPlaylistsGeneratedErrorMessage);
        logger.error(String.format(COULD_NOT_RETRIEVE_PLAYLISTS_MESSAGE, ngp.getMessage()));
        throw ngp;
    }

    private List<PlaylistDTO> mapListOfPlaylistToDTOs(List<Playlist> playlists) {
        return playlists.stream().map(ModelMapper::playlistToDTO).collect(Collectors.toList());
    }
}
