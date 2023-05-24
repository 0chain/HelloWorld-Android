package org.zus.helloworld.models.fees


import com.google.gson.annotations.SerializedName

data class FeesTableModel(
    @SerializedName("6dba10422e368813802877a85039d3985d96760ed844092319743fb3a76712d3")
    var faucet: Faucet,
    @SerializedName("6dba10422e368813802877a85039d3985d96760ed844092319743fb3a76712d7")
    var blobber: Blobber,
    @SerializedName("6dba10422e368813802877a85039d3985d96760ed844092319743fb3a76712d9")
    var sharderMiner: SharderMiner,
    @SerializedName("6dba10422e368813802877a85039d3985d96760ed844092319743fb3a76712e0")
    var dba10422e368813802877a85039d3985d96760ed844092319743fb3a76712e0: Dba10422e368813802877a85039d3985d96760ed844092319743fb3a76712e0,
    @SerializedName("transfer")
    var transfer: Transfer
) {
    data class Faucet(
        @SerializedName("pour")
        var pour: Int,
        @SerializedName("refill")
        var refill: Int,
        @SerializedName("update-settings")
        var updateSettings: Int
    )

    data class Blobber(
        @SerializedName("add_blobber")
        var addBlobber: Int,
        @SerializedName("add_free_storage_assigner")
        var addFreeStorageAssigner: Int,
        @SerializedName("add_validator")
        var addValidator: Int,
        @SerializedName("blobber_block_rewards")
        var blobberBlockRewards: Int,
        @SerializedName("blobber_health_check")
        var blobberHealthCheck: Int,
        @SerializedName("cancel_allocation")
        var cancelAllocation: Int,
        @SerializedName("challenge_request")
        var challengeRequest: Int,
        @SerializedName("challenge_response")
        var challengeResponse: Int,
        @SerializedName("collect_reward")
        var collectReward: Int,
        @SerializedName("commit_connection")
        var commitConnection: Int,
        @SerializedName("commit_settings_changes")
        var commitSettingsChanges: Int,
        @SerializedName("finalize_allocation")
        var finalizeAllocation: Int,
        @SerializedName("free_allocation_request")
        var freeAllocationRequest: Int,
        @SerializedName("free_update_allocation")
        var freeUpdateAllocation: Int,
        @SerializedName("generate_challenge")
        var generateChallenge: Int,
        @SerializedName("kill_blobber")
        var killBlobber: Int,
        @SerializedName("kill_validator")
        var killValidator: Int,
        @SerializedName("new_allocation_request")
        var newAllocationRequest: Int,
        @SerializedName("new_read_pool")
        var newReadPool: Int,
        @SerializedName("pay_blobber_block_rewards")
        var payBlobberBlockRewards: Int,
        @SerializedName("read_pool_lock")
        var readPoolLock: Int,
        @SerializedName("read_pool_unlock")
        var readPoolUnlock: Int,
        @SerializedName("read_redeem")
        var readRedeem: Int,
        @SerializedName("shutdown_blobber")
        var shutdownBlobber: Int,
        @SerializedName("shutdown_validator")
        var shutdownValidator: Int,
        @SerializedName("stake_pool_lock")
        var stakePoolLock: Int,
        @SerializedName("stake_pool_pay_interests")
        var stakePoolPayInterests: Int,
        @SerializedName("stake_pool_unlock")
        var stakePoolUnlock: Int,
        @SerializedName("update_allocation_request")
        var updateAllocationRequest: Int,
        @SerializedName("update_blobber_settings")
        var updateBlobberSettings: Int,
        @SerializedName("update_settings")
        var updateSettings: Int,
        @SerializedName("update_validator_settings")
        var updateValidatorSettings: Int,
        @SerializedName("validator_health_check")
        var validatorHealthCheck: Int,
        @SerializedName("write_pool_lock")
        var writePoolLock: Int,
        @SerializedName("write_pool_unlock")
        var writePoolUnlock: Int
    )

    data class SharderMiner(
        @SerializedName("add_miner")
        var addMiner: Int,
        @SerializedName("add_sharder")
        var addSharder: Int,
        @SerializedName("addtodelegatepool")
        var addtodelegatepool: Int,
        @SerializedName("collect_reward")
        var collectReward: Int,
        @SerializedName("contributempk")
        var contributempk: Int,
        @SerializedName("delete_miner")
        var deleteMiner: Int,
        @SerializedName("delete_sharder")
        var deleteSharder: Int,
        @SerializedName("deletefromdelegatepool")
        var deletefromdelegatepool: Int,
        @SerializedName("feespaid")
        var feespaid: Int,
        @SerializedName("kill_miner")
        var killMiner: Int,
        @SerializedName("kill_sharder")
        var killSharder: Int,
        @SerializedName("miner_health_check")
        var minerHealthCheck: Int,
        @SerializedName("mintedtokens")
        var mintedtokens: Int,
        @SerializedName("payfees")
        var payfees: Int,
        @SerializedName("sharder_health_check")
        var sharderHealthCheck: Int,
        @SerializedName("sharder_keep")
        var sharderKeep: Int,
        @SerializedName("sharesignsorshares")
        var sharesignsorshares: Int,
        @SerializedName("update_globals")
        var updateGlobals: Int,
        @SerializedName("update_miner_settings")
        var updateMinerSettings: Int,
        @SerializedName("update_settings")
        var updateSettings: Int,
        @SerializedName("update_sharder_settings")
        var updateSharderSettings: Int,
        @SerializedName("wait")
        var wait: Int
    )

    data class Dba10422e368813802877a85039d3985d96760ed844092319743fb3a76712e0(
        @SerializedName("add-authorizer")
        var addAuthorizer: Int,
        @SerializedName("authorizer-health-check")
        var authorizerHealthCheck: Int,
        @SerializedName("burn")
        var burn: Int,
        @SerializedName("delete-authorizer")
        var deleteAuthorizer: Int,
        @SerializedName("mint")
        var mint: Int
    )

    data class Transfer(
        @SerializedName("transfer")
        var transfer: Int
    )
}
