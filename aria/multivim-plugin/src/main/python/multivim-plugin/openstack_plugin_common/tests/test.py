import json
import os

from cloudify.context import BootstrapContext

from cloudify.mocks import MockCloudifyContext


RETRY_AFTER = 1
# Time during which no retry could possibly happen.
NO_POSSIBLE_RETRY_TIME = RETRY_AFTER / 2.0

BOOTSTRAP_CONTEXTS_WITHOUT_PREFIX = (
    {
    },
    {
        'resources_prefix': ''
    },
    {
        'resources_prefix': None
    },
)


def set_mock_provider_context(ctx, provider_context):

    def mock_provider_context(provider_name_unused):
        return provider_context

    ctx.get_provider_context = mock_provider_context


def create_mock_ctx_with_provider_info(*args, **kw):
    cur_dir = os.path.dirname(os.path.realpath(__file__))
    full_file_name = os.path.join(cur_dir, 'provider-context.json')
    with open(full_file_name) as f:
        provider_context = json.loads(f.read())['context']
    kw['provider_context'] = provider_context
    kw['bootstrap_context'] = BootstrapContext(provider_context['cloudify'])
    return MockCloudifyContext(*args, **kw)
