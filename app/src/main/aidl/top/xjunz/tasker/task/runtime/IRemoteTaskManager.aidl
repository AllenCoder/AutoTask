// IRemoteTaskManager.aidl
package top.xjunz.tasker.task.runtime;
import top.xjunz.tasker.engine.dto.XTaskDTO;
import top.xjunz.tasker.engine.task.TaskSnapshot;

interface IRemoteTaskManager {

    void initialize(in List<XTaskDTO> carriers);

    boolean isInitialized();

    void updateResidentTask(long previous, in XTaskDTO updated);

    void disableResidentTask(long identifier);

    void enableResidentTask(in XTaskDTO carrier);

    int getSnapshotCount(long identifier);

    TaskSnapshot[] getAllSnapshots(long identifier);
}