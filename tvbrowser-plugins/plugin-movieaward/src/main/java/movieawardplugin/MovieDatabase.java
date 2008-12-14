package movieawardplugin;

import java.util.ArrayList;

import util.misc.SoftReferenceCache;
import devplugin.Program;

public class MovieDatabase {

  private SoftReferenceCache<Program, Movie> mProgramCache = new SoftReferenceCache<Program, Movie>();
  private static final Movie EMPTY_MOVIE = new Movie("##EMPTYMOVIE##");
  private MovieHashMap mMovies = new MovieHashMap();

  public void addMovie(final Movie movie) {
    mMovies.addMovie(movie);
  }

  public Movie getMovieFor(final Program program) {
    if (mProgramCache.containsKey(program)) {
      final Movie movie = mProgramCache.get(program);
      if (movie == EMPTY_MOVIE) {
        return null;
      }
      return movie;
    }

    ArrayList<Movie> movies = mMovies.getMovies(program);
    if (movies != null) {
      for (Movie movie : movies) {
        if (movie.matchesProgram(program)) {
          mProgramCache.put(program, movie);
          return movie;
        }
      }
    }

    mProgramCache.put(program, EMPTY_MOVIE);
    return null;
  }

  public void clear() {
    mProgramCache = new SoftReferenceCache<Program, Movie>();
    mMovies = new MovieHashMap();
  }
}
