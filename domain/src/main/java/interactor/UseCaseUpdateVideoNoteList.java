package interactor;

import database.IDataBase;
import database.VideoIDataBase;
import executor.PostExecutionThread;
import executor.ThreadExecutor;
import model.VideoDomain;
import rx.Observable;

/**
 * Created by Terry on 11/6/2016.
 * Use case for update video note
 */

public class UseCaseUpdateVideoNoteList extends UseCase {

    private final VideoIDataBase mDataBase;
    private final VideoDomain mVideoDomain;

    public UseCaseUpdateVideoNoteList(ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread, VideoDomain videoDomain, IDataBase IDataBase) {
        super(threadExecutor, postExecutionThread);
        mDataBase = (VideoIDataBase) IDataBase;
        mVideoDomain = videoDomain;
    }

    @Override
    public Observable buildUseCaseObservable() {
        return mDataBase.updateVideoList(mVideoDomain);
    }
}
