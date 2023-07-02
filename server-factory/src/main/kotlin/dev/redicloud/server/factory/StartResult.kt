package dev.redicloud.server.factory

import dev.redicloud.repository.server.CloudServer
import dev.redicloud.repository.server.version.CloudServerVersion

open class StartResult(val type: StartResultType)
class SuccessStartResult(val server: CloudServer, val process: ServerProcess) : StartResult(StartResultType.SUCCESS)
class AlreadyRunningStartResult(val server: CloudServer) : StartResult(StartResultType.ALREADY_RUNNING)
class NotEnoughRamOnNodeStartResult() : StartResult(StartResultType.RAM_USAGE_TOO_HIGH)
class NotEnoughRamOnJVMStartResult() : StartResult(StartResultType.RAM_USAGE_TOO_HIGH)
class TooMuchServicesOfTemplateStartResult() : StartResult(StartResultType.TOO_MUCH_SERVICES_OF_TEMPLATE)
class TooMuchServicesOfTemplateOnNodeStartResult(): StartResult(StartResultType.TOO_MUCH_SERVICES_OF_TEMPLATE)
class NodeIsNotAllowedStartResult() : StartResult(StartResultType.NODE_IS_NOT_ALLOWED)
class UnknownServerVersionStartResult(val serviceVersion: CloudServerVersion?) : StartResult(StartResultType.UNKNOWN_SERVER_VERSION)
class UnknownErrorStartResult(val throwable: Throwable) : StartResult(StartResultType.UNKNOWN_ERROR)


enum class StartResultType {
    SUCCESS,
    ALREADY_RUNNING,
    RAM_USAGE_TOO_HIGH,
    TOO_MUCH_SERVICES_OF_TEMPLATE,
    NODE_IS_NOT_ALLOWED,
    NODE_NOT_CONNECTED,
    UNKNOWN_SERVER_VERSION,
    UNKNOWN_ERROR
}