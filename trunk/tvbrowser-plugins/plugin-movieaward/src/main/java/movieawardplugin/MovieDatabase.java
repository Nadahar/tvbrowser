package movieawardplugin;

import util.misc.SoftReferenceCache;

import java.util.ArrayList;

import devplugin.Program;

public class MovieDatabase {

  private SoftReferenceCache<Program, Movie> mProgramCache = new SoftReferenceCache<Program, Movie>();
  private ArrayList<Movie> mMovies = new ArrayList<Movie>();

  public void addMovie(final Movie movie) {
    mMovies.add(movie);
  }

  public Movie getMovieFor(final Program program) {
    if (mProgramCache.containsKey(program)) {
      return mProgramCache.get(program);
    }

    for (final Movie movie : mMovies) {
      if (movie.matchesProgram(program)) {
        mProgramCache.put(program, movie);
        return movie;
      }
    }

    mProgramCache.put(program, null);
    return null;
  }

  public void clear() {
    mProgramCache = new SoftReferenceCache<Program, Movie>();
    mMovies = new ArrayList<Movie>();
  }
}
