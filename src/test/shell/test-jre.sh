#!/usr/bin/env sh

# Run local gitlab runner to test an specified JRE from pre-build agent in /tmp/output
# This script use the docker socket, so the caller should be root or in the docker group.

if [ "$#" != "1" -a "$#" != 2 ]; then
    echo "Usage: $0 JOB_ID [PORT]"
    exit 1
fi
# The first argument is the name of a job as defined in the gitlab CI configuration file.
# The second argument is optional. If set, remote debugging servers are spawn listening on the specified port.

job_id="$1"
debug_port="$2"

debug_opts=$([ -n "$debug_port" ] && echo "--docker-network-mode host --env DEBUG=${debug_port}")

gitlab-runner exec docker \
    ${debug_opts} \
    --pre-build-script "cp -r /cache/ ./target/" \
    --docker-volumes /tmp/output:/cache \
    ${job_id}
