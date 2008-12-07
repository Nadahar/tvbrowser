package movieawardplugin;

import util.misc.SoftReferenceCache;

import java.util.ArrayList;

import devplugin.Program;

public class MovieDatabase {

  private SoftReferenceCache<Program, Movie> mProgramCache = new SoftReferenceCache<Program, Movie>();
  private ArrayList<Movie> mMovies = new ArrayList<Movie>();
  private static final Movie EMPTY_MOVIE = new Movie("##EMPTYMOVIE##");

  public void addMovie(final Movie movie) {
    mMovies.add(movie);
  }

  public Movie getMovieFor(final Program program) {
    if (mProgramCache.containsKey(program)) {
      final Movie movie = mProgramCache.get(program);
      if (movie == EMPTY_MOVIE) {
        return null;
      }
      return mProgramCache.get(program);
    }

    for (final Movie movie : mMovies) {
      if (movie.matchesProgram(program)) {
        mProgramCache.put(program, movie);
        return movie;
      }
    }

    mProgramCache.put(program, EMPTY_MOVIE);
    return null;
  }

  public void clear() {
    mProgramCache = new SoftReferenceCache<Program, Movie>();
    mMovies = new ArrayList<Movie>();
  }
}
