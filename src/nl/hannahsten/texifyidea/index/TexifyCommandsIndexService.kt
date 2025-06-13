package nl.hannahsten.texifyidea.index

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch


@Service(Service.Level.PROJECT)
class TexifyCommandsIndexService(
    val project : Project,
    val myScope: CoroutineScope) {

    private val updateChannel = Channel<Unit>(Channel.CONFLATED)

    init{

        myScope.launch {
            updateChannel.consumeEach {
                withBackgroundProgress(project, "Updating Texify Commands Index") {

                    reportProgress {

                    }


                }
            }
        }

    }
}